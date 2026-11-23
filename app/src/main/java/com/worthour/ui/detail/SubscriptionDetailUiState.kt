package com.worthour.ui.detail

import com.worthour.data.AppEntity
import com.worthour.data.SubscriptionEntity
import com.worthour.data.ValueCalculationResult
import java.time.LocalDate

data class DailyUsageUiModel(
    val date: LocalDate,
    val usageTimeMillis: Long,
    val hasRecord: Boolean
) {
    val usageMinutes: Long
        get() = (usageTimeMillis / 1000) / 60
}

data class SubscriptionDetailUiState(
    val subscription: SubscriptionEntity? = null,
    val valueResult: ValueCalculationResult? = null,
    val linkedApps: List<AppEntity> = emptyList(),
    val dailyUsages: List<DailyUsageUiModel> = emptyList(),
    val totalUsageTimeMillis30Days: Long = 0L,
    val recordedDaysCount: Int = 0,
    val startDate30Days: LocalDate = LocalDate.now().minusDays(30),
    val endDateToday: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
