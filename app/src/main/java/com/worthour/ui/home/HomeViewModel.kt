package com.worthour.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.data.AppDatabase
import com.worthour.data.SubscriptionEntity
import com.worthour.data.SubscriptionRepository
import com.worthour.data.UsageRepository
import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TopAppUsage(
    val packageName: String,
    val usageTimeMillis: Long
)

data class HomeSubscriptionItem(
    val entity: SubscriptionEntity,
    val valueScore: ValueScore = ValueScore.MEASURING
)

data class UpcomingPaymentItem(
    val subscription: SubscriptionEntity,
    val dDayDays: Long
) {
    val dDayText: String
        get() = when {
            dDayDays == 0L -> "D-Day"
            dDayDays > 0 -> "D-$dDayDays"
            else -> "D+${-dDayDays}"
        }
}

data class HomeUiState(
    val totalMonthlySpending: Int = 0,
    val activeSubscriptionsCount: Int = 0,
    val upcomingPayment: UpcomingPaymentItem? = null,
    val subscriptions: List<HomeSubscriptionItem> = emptyList(),
    val permissionWarning: Boolean = false,
    val topApps: List<TopAppUsage> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val usageRepository: UsageRepository
    private val subscriptionRepository: SubscriptionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val usageStatsReader = UsageStatsReader(application)
        usageRepository = UsageRepository(database.usageRecordDao(), usageStatsReader)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())

        loadTodayTopApps()
    }

    private val _topApps = MutableStateFlow<List<TopAppUsage>>(emptyList())
    val topApps: StateFlow<List<TopAppUsage>> = _topApps.asStateFlow()

    private val _permissionWarning = MutableStateFlow(false)
    val permissionWarning: StateFlow<Boolean> = _permissionWarning.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        subscriptionRepository.getAllSubscriptions(),
        _permissionWarning,
        _topApps
    ) { subs, permWarning, apps ->
        val totalSpending = subs.sumOf { it.price }
        val activeCount = subs.size

        val today = LocalDate.now()
        val upcoming = subs.filter { !it.nextPaymentDate.isBefore(today) }
            .minByOrNull { it.nextPaymentDate }
            ?.let { sub ->
                val days = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)
                UpcomingPaymentItem(sub, days)
            }

        val items = subs.map { HomeSubscriptionItem(entity = it, valueScore = ValueScore.MEASURING) }

        HomeUiState(
            totalMonthlySpending = totalSpending,
            activeSubscriptionsCount = activeCount,
            upcomingPayment = upcoming,
            subscriptions = items,
            permissionWarning = permWarning,
            topApps = apps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun loadTodayTopApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val usageMap = usageRepository.getUsageForDate(LocalDate.now())
            if (usageMap.isEmpty()) {
                _permissionWarning.value = true
                _topApps.value = emptyList()
            } else {
                _permissionWarning.value = false
                val sorted = usageMap.map { TopAppUsage(it.key, it.value) }
                    .sortedByDescending { it.usageTimeMillis }
                    .take(5)
                _topApps.value = sorted
            }
        }
    }
}
