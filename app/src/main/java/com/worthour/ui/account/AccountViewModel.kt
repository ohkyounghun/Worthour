package com.worthour.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.worthour.data.AppDatabase
import com.worthour.data.ShareableSubscription
import com.worthour.data.SubscriptionJsonConverter
import com.worthour.data.SubscriptionRepository
import com.worthour.notification.PaymentNotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AccountAuthMessage {
    data object EmptyFields : AccountAuthMessage
    data object LoginSuccess : AccountAuthMessage
    data object SignupSuccess : AccountAuthMessage
    data object LogoutSuccess : AccountAuthMessage
    data object BackupSuccess : AccountAuthMessage
    data object RestoreSuccess : AccountAuthMessage
    data object NoBackupFound : AccountAuthMessage
    data class AuthFailed(val errorMessage: String) : AccountAuthMessage
}

data class AccountUiState(
    val currentUser: FirebaseUser? = null,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val authMessage: AccountAuthMessage? = null
)

class AccountViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val subscriptionRepository: SubscriptionRepository

    private val _uiState = MutableStateFlow(AccountUiState(currentUser = auth.currentUser))
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _uiState.value = _uiState.value.copy(
            currentUser = firebaseAuth.currentUser,
            isLoading = false
        )
    }

    init {
        val database = AppDatabase.getDatabase(application)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun onEmailInputChanged(email: String) {
        _uiState.value = _uiState.value.copy(emailInput = email)
    }

    fun onPasswordInputChanged(password: String) {
        _uiState.value = _uiState.value.copy(passwordInput = password)
    }

    fun clearAuthMessage() {
        _uiState.value = _uiState.value.copy(authMessage = null)
    }

    fun signIn() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(authMessage = AccountAuthMessage.EmptyFields)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailInput = "",
                    passwordInput = "",
                    authMessage = AccountAuthMessage.LoginSuccess
                )
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    authMessage = AccountAuthMessage.AuthFailed(exception.localizedMessage ?: "Unknown error")
                )
            }
    }

    fun signUp() {
        val email = _uiState.value.emailInput.trim()
        val password = _uiState.value.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(authMessage = AccountAuthMessage.EmptyFields)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailInput = "",
                    passwordInput = "",
                    authMessage = AccountAuthMessage.SignupSuccess
                )
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    authMessage = AccountAuthMessage.AuthFailed(exception.localizedMessage ?: "Unknown error")
                )
            }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = _uiState.value.copy(
            authMessage = AccountAuthMessage.LogoutSuccess
        )
    }

    fun backupSubscriptions() {
        val user = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val subs = subscriptionRepository.getAllSubscriptions().first()
                val shareableList = subs.map { sub ->
                    val linkedApps = subscriptionRepository.getLinkedApps(sub.id).first()
                    val packageNames = linkedApps.map { it.packageName }
                    ShareableSubscription.fromEntity(sub, packageNames)
                }

                val jsonString = SubscriptionJsonConverter.toJson(shareableList)

                val docRef = firestore.collection("users").document(user.uid)
                val data = mapOf(
                    "subscriptionsJson" to jsonString,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                docRef.set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            authMessage = AccountAuthMessage.BackupSuccess
                        )
                    }
                    .addOnFailureListener { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            authMessage = AccountAuthMessage.AuthFailed(exception.localizedMessage ?: "Backup failed")
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    authMessage = AccountAuthMessage.AuthFailed(e.localizedMessage ?: "Backup failed")
                )
            }
        }
    }

    fun restoreSubscriptions() {
        val user = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)

        val docRef = firestore.collection("users").document(user.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                val jsonString = document.getString("subscriptionsJson")
                if (jsonString.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        authMessage = AccountAuthMessage.NoBackupFound
                    )
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val shareableList = SubscriptionJsonConverter.listFromJson(jsonString)

                            // Cancel notifications for existing local subscriptions
                            val oldSubs = subscriptionRepository.getAllSubscriptions().first()
                            oldSubs.forEach { oldSub ->
                                PaymentNotificationScheduler.cancelNotification(getApplication(), oldSub.id)
                            }

                            // Clear existing subscriptions from DB
                            subscriptionRepository.deleteAllSubscriptions()

                            // Insert restored subscriptions and schedule notifications
                            shareableList.forEach { shareable ->
                                val entity = shareable.toSubscriptionEntity(id = 0L)
                                val newSubId = subscriptionRepository.saveSubscriptionWithApps(entity, shareable.linkedAppPackageNames)
                                val restoredEntity = entity.copy(id = newSubId)
                                PaymentNotificationScheduler.scheduleNotification(getApplication(), restoredEntity)
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                authMessage = AccountAuthMessage.RestoreSuccess
                            )
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                authMessage = AccountAuthMessage.AuthFailed(e.localizedMessage ?: "Restore failed")
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    authMessage = AccountAuthMessage.AuthFailed(exception.localizedMessage ?: "Restore failed")
                )
            }
    }
}
