package com.worthour.data

import com.worthour.ui.home.ValueScore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class GetSubscriptionValueUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val usageRepository: UsageRepository
) {

    /**
     * Calculates the ValueCalculationResult (costPerHour & valueScore) for a single subscription reactively.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSubscriptionValueFlow(
        subscription: SubscriptionEntity,
        today: LocalDate = LocalDate.now()
    ): Flow<ValueCalculationResult> {
        if (subscription.analysisType == AnalysisType.NON_TIME_TRACKING) {
            return flowOf(ValueCalculationResult(costPerHour = null, valueScore = ValueScore.NOT_TRACKED))
        }

        val startDate = today.minusDays(30)
        val recordedDaysCountFlow = usageRepository.getRecordedDaysCountFlow(startDate)
        val linkedAppsFlow = subscriptionRepository.getLinkedApps(subscription.id)

        return linkedAppsFlow.flatMapLatest { linkedApps ->
            val packageNames = linkedApps.map { it.packageName }
            val totalUsageFlow = usageRepository.getTotalUsageTimeForPackagesFlow(packageNames, startDate)

            combine(totalUsageFlow, recordedDaysCountFlow) { totalUsageMillis, recordedDaysCount ->
                ValueScoreCalculator.calculate(
                    subscription = subscription,
                    totalUsageTimeMillis = totalUsageMillis,
                    recordedDaysCount = recordedDaysCount
                )
            }
        }
    }

    /**
     * Calculates SubscriptionValueItem for all subscriptions reactively.
     * Reuses getSubscriptionValueFlow for each subscription to avoid duplicate calculation logic.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllSubscriptionValuesFlow(
        todayFlow: Flow<LocalDate>
    ): Flow<List<SubscriptionValueItem>> {
        return combine(
            subscriptionRepository.getAllSubscriptions(),
            todayFlow
        ) { subs, today ->
            subs to today
        }.flatMapLatest { (subs, today) ->
            if (subs.isEmpty()) {
                flowOf(emptyList())
            } else {
                val itemFlows: List<Flow<SubscriptionValueItem>> = subs.map { sub ->
                    getSubscriptionValueFlow(sub, today).map { valueResult ->
                        SubscriptionValueItem(subscription = sub, valueResult = valueResult)
                    }
                }

                combine(itemFlows) { itemsArray ->
                    itemsArray.toList()
                }
            }
        }
    }
}
