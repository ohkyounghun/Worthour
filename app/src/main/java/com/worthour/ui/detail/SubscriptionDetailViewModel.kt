package com.worthour.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.data.AppDatabase
import com.worthour.data.GetSubscriptionValueUseCase
import com.worthour.data.SubscriptionRepository
import com.worthour.data.UsageRepository
import com.worthour.notification.PaymentNotificationScheduler
import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SubscriptionDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository: SubscriptionRepository
    private val usageRepository: UsageRepository
    private val valueUseCase: GetSubscriptionValueUseCase

    init {
        val database = AppDatabase.getDatabase(application)
        val usageStatsReader = UsageStatsReader(application)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        usageRepository = UsageRepository(database.usageRecordDao(), usageStatsReader)
        valueUseCase = GetSubscriptionValueUseCase(subscriptionRepository, usageRepository)
    }

    private val _subscriptionId = MutableStateFlow<Long?>(null)
    private val _isDeleted = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SubscriptionDetailUiState> = combine(
        _subscriptionId,
        _isDeleted
    ) { id, deleted ->
        id to deleted
    }.flatMapLatest { (id, deleted) ->
        if (id == null || id <= 0L || deleted) {
            flowOf(SubscriptionDetailUiState(isLoading = false, isDeleted = deleted))
        } else {
            subscriptionRepository.getSubscriptionById(id).flatMapLatest { sub ->
                if (sub == null) {
                    flowOf(SubscriptionDetailUiState(isLoading = false, errorMessage = "Subscription not found."))
                } else {
                    val today = LocalDate.now()
                    val endDateYesterday = today.minusDays(1)
                    val startDate30 = today.minusDays(30)
                    val valueFlow = valueUseCase.getSubscriptionValueFlow(sub, today)
                    val linkedAppsFlow = subscriptionRepository.getLinkedApps(sub.id)

                    combine(valueFlow, linkedAppsFlow) { valueResult, linkedApps ->
                        sub to (valueResult to linkedApps)
                    }.flatMapLatest { (subscription, valueAndApps) ->
                        val (valueResult, linkedApps) = valueAndApps
                        val packageNames = linkedApps.map { it.packageName }
                        val usageFlow = usageRepository.getUsageRecordsForPackages(packageNames, startDate30, endDateYesterday)

                        usageFlow.map { records ->
                            // Calculate daily usages for last 30 days (up to yesterday) in reverse chronological order
                            val last30DaysUpToYesterday = (1L..30L).map { today.minusDays(it) }
                            val recordsByDate = records.groupBy { it.date }

                            val dailyList = last30DaysUpToYesterday.map { date ->
                                val dateRecords = recordsByDate[date]
                                if (dateRecords != null && dateRecords.isNotEmpty()) {
                                    val totalMillis = dateRecords.sumOf { it.usageTimeMillis }
                                    DailyUsageUiModel(date = date, usageTimeMillis = totalMillis, hasRecord = true)
                                } else {
                                    DailyUsageUiModel(date = date, usageTimeMillis = 0L, hasRecord = false)
                                }
                            }

                            val total30Millis = records.sumOf { it.usageTimeMillis }
                            val recDays = records.map { it.date }.distinct().size

                            SubscriptionDetailUiState(
                                subscription = subscription,
                                valueResult = valueResult,
                                linkedApps = linkedApps,
                                dailyUsages = dailyList,
                                totalUsageTimeMillis30Days = total30Millis,
                                recordedDaysCount = recDays,
                                startDate30Days = startDate30,
                                endDateToday = endDateYesterday,
                                isLoading = false,
                                isDeleted = false
                            )
                        }
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionDetailUiState(isLoading = true)
    )

    fun loadSubscription(subscriptionId: Long) {
        if (subscriptionId <= 0L) return
        _subscriptionId.value = subscriptionId
    }

    fun dismissErrorMessage() {
        _subscriptionId.value?.let { id ->
            _subscriptionId.value = id
        }
    }

    fun deleteSubscription(subscriptionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete from DB FIRST
                subscriptionRepository.deleteSubscriptionById(subscriptionId)
                // AFTER DB deletion succeeds, cancel notification reservation
                PaymentNotificationScheduler.cancelNotification(getApplication(), subscriptionId)
                _isDeleted.value = true
            } catch (e: Exception) {
                // On failure, show error message and do NOT cancel notification
                _subscriptionId.value?.let { id ->
                    _subscriptionId.value = id
                }
            }
        }
    }
}
