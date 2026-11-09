package com.worthour.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.data.AppDatabase
import com.worthour.data.AppEntity
import com.worthour.data.SubscriptionEntity
import com.worthour.data.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionDetailUiState(
    val subscription: SubscriptionEntity? = null,
    val linkedApps: List<AppEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class SubscriptionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SubscriptionRepository(database.subscriptionDao())
    }

    private val _uiState = MutableStateFlow(SubscriptionDetailUiState())
    val uiState: StateFlow<SubscriptionDetailUiState> = _uiState.asStateFlow()

    fun loadSubscription(subscriptionId: Long) {
        if (subscriptionId <= 0L) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val sub = repository.getSubscriptionById(subscriptionId).firstOrNull()
            if (sub != null) {
                val apps = repository.getLinkedApps(subscriptionId).firstOrNull() ?: emptyList()
                _uiState.update { it.copy(subscription = sub, linkedApps = apps, isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Subscription not found.") }
            }
        }
    }

    fun deleteSubscription(subscriptionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteSubscriptionById(subscriptionId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "An error occurred while deleting.") }
            }
        }
    }
}
