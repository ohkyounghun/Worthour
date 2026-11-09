package com.worthour.ui.add

import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import java.time.LocalDate

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean = true
)

data class AddSubscriptionUiState(
    val subscriptionId: Long = 0L,
    val isEditMode: Boolean = false,
    val selectedPresetId: String? = null,
    val serviceName: String = "",
    val priceText: String = "",
    val cycle: PaymentCycle = PaymentCycle.MONTHLY,
    val nextPaymentDate: LocalDate = LocalDate.now().plusMonths(1),
    val hasFreeTrial: Boolean = false,
    val freeTrialEndDate: LocalDate? = null,
    val analysisType: AnalysisType = AnalysisType.TIME_TRACKING,
    val linkedPackages: List<String> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val serviceNameError: String? = null,
    val priceError: String? = null,
    val trialDateError: String? = null,
    val validationErrors: List<String> = emptyList(),
    val showValidationErrorDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)
