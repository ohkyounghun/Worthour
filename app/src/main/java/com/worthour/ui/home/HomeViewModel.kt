package com.worthour.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.R
import com.worthour.data.AppDatabase
import com.worthour.data.Currency
import com.worthour.data.CurrencyConverter
import com.worthour.data.ExchangeRateRepository
import com.worthour.data.GetSubscriptionValueUseCase
import com.worthour.data.PaymentCycle
import com.worthour.data.SubscriptionEntity
import com.worthour.data.SubscriptionRepository
import com.worthour.data.SubscriptionValueItem
import com.worthour.data.UsageRepository
import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.round

object HomePrefs {
    private const val PREFS_NAME = "worthour_home_prefs"
    private const val KEY_DISPLAY_CURRENCY = "spending_display_currency"

    fun getDisplayCurrency(context: Context): Currency {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_DISPLAY_CURRENCY, Currency.KRW.name)
        return try { Currency.valueOf(name!!) } catch (_: Exception) { Currency.KRW }
    }

    fun setDisplayCurrency(context: Context, currency: Currency) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DISPLAY_CURRENCY, currency.name).apply()
    }
}

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
            else -> "Date passed"
        }
}

data class HomeUiState(
    val totalMonthlySpending: Int = 0,
    val totalMonthlySpendingText: String = "₩0",
    val selectedDisplayCurrency: Currency = Currency.KRW,
    val isCurrencyToggleEnabled: Boolean = false,
    val activeSubscriptionsCount: Int = 0,
    val upcomingPayment: UpcomingPaymentItem? = null,
    val subscriptions: List<HomeSubscriptionItem> = emptyList(),
    val permissionWarning: Boolean = false,
    val topApps: List<TopAppUsage> = emptyList(),
    val isLoading: Boolean = false
)

private data class HomeDataTuple(
    val subValueItems: List<SubscriptionValueItem>,
    val permWarning: Boolean,
    val apps: List<TopAppUsage>,
    val today: LocalDate
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val usageRepository: UsageRepository
    private val subscriptionRepository: SubscriptionRepository
    private val getSubscriptionValueUseCase: GetSubscriptionValueUseCase

    private val _displayCurrency = MutableStateFlow(HomePrefs.getDisplayCurrency(application))
    val displayCurrency: StateFlow<Currency> = _displayCurrency.asStateFlow()

    private val _topApps = MutableStateFlow<List<TopAppUsage>>(emptyList())
    val topApps: StateFlow<List<TopAppUsage>> = _topApps.asStateFlow()

    private val _permissionWarning = MutableStateFlow(false)
    val permissionWarning: StateFlow<Boolean> = _permissionWarning.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())

    init {
        val database = AppDatabase.getDatabase(application)
        val usageStatsReader = UsageStatsReader(application)
        val exchangeRateRepo = ExchangeRateRepository(application)
        usageRepository = UsageRepository(database.usageRecordDao(), usageStatsReader)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        getSubscriptionValueUseCase = GetSubscriptionValueUseCase(subscriptionRepository, usageRepository, exchangeRateRepo)

        loadTodayTopApps()

        // Periodic check for date change (midnight passing while app is open)
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val now = LocalDate.now()
                if (now != _currentDate.value) {
                    _currentDate.value = now
                }
                delay(60_000L)
            }
        }
    }

    fun refreshToday() {
        val now = LocalDate.now()
        if (now != _currentDate.value) {
            _currentDate.value = now
        }
    }

    fun onToggleDisplayCurrency(currency: Currency) {
        val rate = ExchangeRateRepository(getApplication<Application>()).getSavedUsdToKrwRate()
        if (rate == null) return
        _displayCurrency.value = currency
        HomePrefs.setDisplayCurrency(getApplication(), currency)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            getSubscriptionValueUseCase.getAllSubscriptionValuesFlow(_currentDate),
            _permissionWarning,
            _topApps,
            _currentDate
        ) { subValueItems, permWarning, apps, today ->
            HomeDataTuple(subValueItems, permWarning, apps, today)
        },
        _displayCurrency,
        ExchangeRateRepository(getApplication<Application>()).getSavedUsdToKrwRateFlow()
    ) { dataTuple, selectedCurrency, rate ->
        val (subValueItems, permWarning, apps, today) = dataTuple
        val subs = subValueItems.map { it.subscription }

        // 1. Monthly Equivalent Total calculation
        // Exclude subs where free trial is ongoing (today is before freeTrialEndDate)
        val activeForSpending = subs.filter { sub ->
            val trialEnd = sub.freeTrialEndDate
            trialEnd == null || !today.isBefore(trialEnd)
        }

        val isToggleEnabled = (rate != null)
        val activeCurrency = if (isToggleEnabled) selectedCurrency else Currency.KRW

        val spendingText = if (isToggleEnabled && rate != null) {
            if (activeCurrency == Currency.KRW) {
                val krwTotal = activeForSpending.sumOf { sub ->
                    CurrencyConverter.toMonthlyPrice(
                        price = sub.price,
                        fromCurrency = sub.currency,
                        targetCurrency = Currency.KRW,
                        cycle = sub.cycle,
                        usdToKrwRate = rate
                    ) ?: 0.0
                }
                String.format(java.util.Locale.US, "₩%,d", round(krwTotal).toInt())
            } else {
                val usdTotal = activeForSpending.sumOf { sub ->
                    CurrencyConverter.toMonthlyPrice(
                        price = sub.price,
                        fromCurrency = sub.currency,
                        targetCurrency = Currency.USD,
                        cycle = sub.cycle,
                        usdToKrwRate = rate
                    ) ?: 0.0
                }
                String.format(java.util.Locale.US, "$%.2f", usdTotal)
            }
        } else {
            var krwTotal = 0.0
            var usdTotal = 0.0
            for (sub in activeForSpending) {
                if (sub.currency == Currency.USD) {
                    val monthlyUsd = CurrencyConverter.toMonthlyPrice(
                        price = sub.price,
                        fromCurrency = Currency.USD,
                        targetCurrency = Currency.USD,
                        cycle = sub.cycle,
                        usdToKrwRate = null
                    ) ?: 0.0
                    usdTotal += monthlyUsd
                } else {
                    val monthlyKrw = CurrencyConverter.toMonthlyPrice(
                        price = sub.price,
                        fromCurrency = Currency.KRW,
                        targetCurrency = Currency.KRW,
                        cycle = sub.cycle,
                        usdToKrwRate = null
                    ) ?: 0.0
                    krwTotal += monthlyKrw
                }
            }
            when {
                usdTotal > 0.0 && krwTotal > 0.0 -> String.format(java.util.Locale.US, "₩%,d + $%.2f", round(krwTotal).toInt(), usdTotal)
                usdTotal > 0.0 -> String.format(java.util.Locale.US, "$%.2f", usdTotal)
                else -> String.format(java.util.Locale.US, "₩%,d", round(krwTotal).toInt())
            }
        }

        val activeCount = subs.size

        // 2. Upcoming Payment (today or future), sorted by nextPaymentDate ascending
        val upcoming = subs.filter { !it.nextPaymentDate.isBefore(today) }
            .minByOrNull { it.nextPaymentDate }
            ?.let { sub ->
                val days = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)
                UpcomingPaymentItem(sub, days)
            }

        // 3. Subscription items sorted by nextPaymentDate
        val sortedHomeItems = subValueItems
            .sortedBy { it.subscription.nextPaymentDate }
            .map { item ->
                HomeSubscriptionItem(
                    entity = item.subscription,
                    valueScore = item.valueScore
                )
            }

        HomeUiState(
            totalMonthlySpendingText = spendingText,
            selectedDisplayCurrency = activeCurrency,
            isCurrencyToggleEnabled = isToggleEnabled,
            activeSubscriptionsCount = activeCount,
            upcomingPayment = upcoming,
            subscriptions = sortedHomeItems,
            permissionWarning = permWarning,
            topApps = apps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    ).stateIn(
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

    fun runManualSync() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = com.worthour.usage.UsageSyncManager.syncNow(getApplication())
            val exchangeRateRepo = ExchangeRateRepository(getApplication<Application>())
            val rate = exchangeRateRepo.getSavedUsdToKrwRate()
            val message = if (rate != null) {
                getApplication<Application>().getString(R.string.home_sync_result_message_with_rate, count, rate.toString())
            } else {
                getApplication<Application>().getString(R.string.home_sync_result_message, count)
            }
            _syncStatus.value = message
        }
    }
}
