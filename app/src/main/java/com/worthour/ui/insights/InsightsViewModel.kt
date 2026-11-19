package com.worthour.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.worthour.data.AppDatabase
import com.worthour.data.GetSubscriptionValueUseCase
import com.worthour.data.SubscriptionRepository
import com.worthour.data.UsageRepository
import com.worthour.ui.home.ValueScore
import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CostPerHourChartItem(
    val subscriptionId: Long,
    val subscriptionName: String,
    val costPerHour: Double,
    val valueScore: ValueScore,
    val maxCostPerHour: Double
)

data class InsightsUiState(
    val chartItems: List<CostPerHourChartItem> = emptyList(),
    val isLoading: Boolean = false
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val usageRepository: UsageRepository
    private val subscriptionRepository: SubscriptionRepository
    private val getSubscriptionValueUseCase: GetSubscriptionValueUseCase

    private val _currentDate = MutableStateFlow(LocalDate.now())

    init {
        val database = AppDatabase.getDatabase(application)
        val usageStatsReader = UsageStatsReader(application)
        usageRepository = UsageRepository(database.usageRecordDao(), usageStatsReader)
        subscriptionRepository = SubscriptionRepository(database.subscriptionDao())
        getSubscriptionValueUseCase = GetSubscriptionValueUseCase(subscriptionRepository, usageRepository)

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

    val uiState: StateFlow<InsightsUiState> = getSubscriptionValueUseCase
        .getAllSubscriptionValuesFlow(_currentDate)
        .combine(_currentDate) { subValueItems, _ ->
            // Exclude MEASURING and NOT_TRACKED
            val validItems = subValueItems.mapNotNull { item ->
                val cost = item.costPerHour
                if (item.valueScore != ValueScore.MEASURING &&
                    item.valueScore != ValueScore.NOT_TRACKED &&
                    cost != null &&
                    !cost.isNaN()
                ) {
                    item to cost
                } else null
            }

            // Sort descending by costPerHour (Double.POSITIVE_INFINITY goes to top)
            val sortedItems = validItems.sortedByDescending { it.second }

            val finiteCosts = sortedItems.mapNotNull { (_, cost) ->
                if (!cost.isInfinite()) cost else null
            }

            val maxCost = finiteCosts.maxOrNull() ?: 1.0

            val chartItems = sortedItems.map { (item, cost) ->
                CostPerHourChartItem(
                    subscriptionId = item.subscription.id,
                    subscriptionName = item.subscription.name,
                    costPerHour = cost,
                    valueScore = item.valueScore,
                    maxCostPerHour = maxCost
                )
            }

            InsightsUiState(
                chartItems = chartItems,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsUiState(isLoading = true)
        )
}
