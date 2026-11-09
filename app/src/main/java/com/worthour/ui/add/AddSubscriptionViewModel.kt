package com.worthour.ui.add

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.R
import com.worthour.data.AnalysisType
import com.worthour.data.AppDatabase
import com.worthour.data.PaymentCycle
import com.worthour.data.PresetSubscription
import com.worthour.data.SubscriptionEntity
import com.worthour.data.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class AddSubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SubscriptionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SubscriptionRepository(database.subscriptionDao())
        loadInstalledApps()
    }

    private val _uiState = MutableStateFlow(AddSubscriptionUiState())
    val uiState: StateFlow<AddSubscriptionUiState> = _uiState.asStateFlow()

    fun initializeForEdit(subscriptionId: Long) {
        if (subscriptionId <= 0L || _uiState.value.isEditMode) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, subscriptionId = subscriptionId, isEditMode = true) }
            val sub = repository.getSubscriptionById(subscriptionId).firstOrNull()
            if (sub != null) {
                val linkedApps = repository.getLinkedApps(subscriptionId).firstOrNull() ?: emptyList()
                val linkedPkgs = linkedApps.map { it.packageName }
                val hasTrial = sub.freeTrialEndDate != null

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        serviceName = sub.name,
                        priceText = sub.price.toString(),
                        cycle = sub.cycle,
                        nextPaymentDate = sub.nextPaymentDate,
                        hasFreeTrial = hasTrial,
                        freeTrialEndDate = sub.freeTrialEndDate,
                        analysisType = sub.analysisType,
                        linkedPackages = linkedPkgs
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectPreset(preset: PresetSubscription) {
        val installedPkgs = _uiState.value.installedApps.map { it.packageName }.toSet()
        // Only auto-link candidate packages that are actually installed on the device
        val autoLinked = preset.candidatePackages.filter { installedPkgs.contains(it) }

        _uiState.update { state ->
            state.copy(
                selectedPresetId = preset.id,
                serviceName = preset.name,
                priceText = preset.defaultPrice.toString(),
                cycle = preset.cycle,
                analysisType = preset.analysisType,
                linkedPackages = autoLinked,
                serviceNameError = null,
                priceError = null
            )
        }
    }

    fun selectCustomSubscription() {
        _uiState.update { state ->
            state.copy(
                selectedPresetId = null,
                serviceName = "",
                priceText = "",
                serviceNameError = null,
                priceError = null
            )
        }
    }

    fun updateServiceName(name: String) {
        _uiState.update { it.copy(serviceName = name, serviceNameError = null) }
    }

    fun updatePrice(priceStr: String) {
        val cleanDigits = priceStr.filter { it.isDigit() }
        val trimmed = if (cleanDigits.length > 9) cleanDigits.take(9) else cleanDigits
        _uiState.update { it.copy(priceText = trimmed, priceError = null) }
    }

    fun updateCycle(cycle: PaymentCycle) {
        _uiState.update { it.copy(cycle = cycle) }
    }

    fun updateNextPaymentDate(date: LocalDate) {
        _uiState.update { state ->
            if (state.hasFreeTrial) {
                state.copy(nextPaymentDate = date, freeTrialEndDate = date, trialDateError = null)
            } else {
                state.copy(nextPaymentDate = date)
            }
        }
    }

    fun toggleFreeTrial(enabled: Boolean) {
        _uiState.update { state ->
            if (enabled) {
                val trialDate = state.freeTrialEndDate ?: state.nextPaymentDate
                state.copy(hasFreeTrial = true, freeTrialEndDate = trialDate, nextPaymentDate = trialDate, trialDateError = null)
            } else {
                state.copy(hasFreeTrial = false, freeTrialEndDate = null, trialDateError = null)
            }
        }
    }

    fun updateFreeTrialEndDate(date: LocalDate?) {
        _uiState.update { state ->
            if (date != null) {
                state.copy(hasFreeTrial = true, freeTrialEndDate = date, nextPaymentDate = date, trialDateError = null)
            } else {
                state.copy(hasFreeTrial = false, freeTrialEndDate = null, trialDateError = null)
            }
        }
    }

    fun updateAnalysisType(type: AnalysisType) {
        _uiState.update { it.copy(analysisType = type) }
    }

    fun addLinkedPackage(packageName: String) {
        _uiState.update { state ->
            if (!state.linkedPackages.contains(packageName)) {
                state.copy(linkedPackages = state.linkedPackages + packageName)
            } else state
        }
    }

    fun removeLinkedPackage(packageName: String) {
        _uiState.update { state ->
            state.copy(linkedPackages = state.linkedPackages.filter { it != packageName })
        }
    }

    fun dismissValidationErrorDialog() {
        _uiState.update { it.copy(showValidationErrorDialog = false) }
    }

    fun saveSubscription() {
        val state = _uiState.value
        val nameTrimmed = state.serviceName.trim()
        val priceInt = state.priceText.toIntOrNull()

        val errors = mutableListOf<String>()
        var nameErr: String? = null
        var priceErr: String? = null

        val context: Context = getApplication()

        if (nameTrimmed.isBlank()) {
            nameErr = context.getString(R.string.error_service_name_blank)
            errors.add(nameErr)
        }

        if (priceInt == null) {
            priceErr = context.getString(R.string.error_price_invalid)
            errors.add(priceErr)
        } else if (priceInt < 1 || priceInt > 100_000_000) {
            priceErr = context.getString(R.string.error_price_out_of_range)
            errors.add(priceErr)
        }

        if (state.hasFreeTrial && state.freeTrialEndDate == null) {
            val trialErr = context.getString(R.string.error_trial_date_missing)
            errors.add(trialErr)
        }

        if (state.analysisType == AnalysisType.TIME_TRACKING && state.linkedPackages.isEmpty()) {
            val appErr = context.getString(R.string.error_time_tracking_no_apps)
            errors.add(appErr)
        }

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    serviceNameError = nameErr,
                    priceError = priceErr,
                    validationErrors = errors,
                    showValidationErrorDialog = true
                )
            }
            return
        }

        val entity = SubscriptionEntity(
            id = state.subscriptionId,
            name = nameTrimmed,
            price = priceInt!!,
            nextPaymentDate = if (state.hasFreeTrial && state.freeTrialEndDate != null) state.freeTrialEndDate else state.nextPaymentDate,
            freeTrialEndDate = if (state.hasFreeTrial) state.freeTrialEndDate else null,
            cycle = state.cycle,
            analysisType = state.analysisType
        )

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveSubscriptionWithApps(entity, state.linkedPackages)
                _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save. Please try again.") }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val context: Context = getApplication()
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val apps = resolveInfos.mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                if (pkg.isNotBlank()) {
                    InstalledAppInfo(packageName = pkg, appName = label, isInstalled = true)
                } else null
            }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase(Locale.getDefault()) }

            _uiState.update { it.copy(installedApps = apps) }
        }
    }
}
