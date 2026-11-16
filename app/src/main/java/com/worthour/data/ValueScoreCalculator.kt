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
     */
    fun calculate(
        subscription: SubscriptionEntity,
        totalUsageTimeMillis: Long,
        recordedDaysCount: Int
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

        // 무료체험 중인 구독도 체험 종료 후 실제로 납부할 요금 기준으로 가치를 보여주기 위해 정가 그대로 계산한다.
        val monthlyPrice = if (subscription.cycle == PaymentCycle.YEARLY) {
            subscription.price.toDouble() / 12.0
        } else {
            subscription.price.toDouble()
        }

        // 측정 일수(최대 30일)에 비례하여 월 요금을 안분 계산
        val clampedRecordedDays = minOf(recordedDaysCount, 30)
        val proRatedPrice = monthlyPrice * (clampedRecordedDays.toDouble() / 30.0)

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
