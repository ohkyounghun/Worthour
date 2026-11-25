package com.worthour.data

import com.worthour.ui.home.ValueScore

data class ValueCalculationResult(
    val costPerHour: Double?,
    val valueScore: ValueScore
)

object ValueScoreCalculator {

    /**
     * Calculates the cost per hour and ValueScore for a subscription based on issue #18 rules.
     *
     * @param subscription The subscription entity.
     * @param totalUsageTimeMillis Sum of usage time in milliseconds for all linked apps in the last 30 days.
     * @param recordedDaysCount Count of distinct recorded days in usage_records in the last 30 days.
     * @param usdToKrwRate Exchange rate from USD to KRW. If null and subscription is USD, returns MEASURING.
     */
    fun calculate(
        subscription: SubscriptionEntity,
        totalUsageTimeMillis: Long,
        recordedDaysCount: Int,
        usdToKrwRate: Double? = null
    ): ValueCalculationResult {
        if (subscription.analysisType == AnalysisType.NON_TIME_TRACKING) {
            return ValueCalculationResult(
                costPerHour = null,
                valueScore = ValueScore.NOT_TRACKED
            )
        }

        if (recordedDaysCount < 7) {
            return ValueCalculationResult(
                costPerHour = null,
                valueScore = ValueScore.MEASURING
            )
        }

        val monthlyPriceInKrw = CurrencyConverter.toMonthlyKrw(
            price = subscription.price,
            currency = subscription.currency,
            cycle = subscription.cycle,
            usdToKrwRate = usdToKrwRate
        ) ?: return ValueCalculationResult(
            costPerHour = null,
            valueScore = ValueScore.MEASURING
        )

        // 측정 일수(최대 30일)에 비례하여 월 요금을 안분 계산
        val clampedRecordedDays = minOf(recordedDaysCount, 30)
        val proRatedPrice = monthlyPriceInKrw * (clampedRecordedDays.toDouble() / 30.0)

        val usageHours = totalUsageTimeMillis.toDouble() / (1000.0 * 60.0 * 60.0)

        if (totalUsageTimeMillis <= 0 || usageHours <= 0.0) {
            return ValueCalculationResult(
                costPerHour = Double.POSITIVE_INFINITY,
                valueScore = ValueScore.LOW
            )
        }

        val costPerHour = proRatedPrice / usageHours

        val score = when {
            costPerHour < 1000.0 -> ValueScore.GOOD
            costPerHour <= 2000.0 -> ValueScore.OK
            else -> ValueScore.LOW
        }

        return ValueCalculationResult(
            costPerHour = costPerHour,
            valueScore = score
        )
    }
}
