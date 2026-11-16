package com.worthour.data

import com.worthour.ui.home.ValueScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValueScoreCalculatorTest {

    private fun createSubscription(
        price: Int = 17000,
        cycle: PaymentCycle = PaymentCycle.MONTHLY,
        analysisType: AnalysisType = AnalysisType.TIME_TRACKING,
        freeTrialEndDate: LocalDate? = null
    ): SubscriptionEntity {
        return SubscriptionEntity(
            id = 1L,
            name = "Test Subscription",
            price = price,
            nextPaymentDate = LocalDate.now().plusDays(10),
            freeTrialEndDate = freeTrialEndDate,
            cycle = cycle,
            analysisType = analysisType
        )
    }

    @Test
    fun `when recorded days count is less than 7, value score is MEASURING`() {
        val subscription = createSubscription()
        val result = ValueScoreCalculator.calculate(
            subscription = subscription,
            totalUsageTimeMillis = 3_600_000L * 10,
            recordedDaysCount = 6
        )

        assertEquals(ValueScore.MEASURING, result.valueScore)
        assertNull(result.costPerHour)
    }

    @Test
    fun `yearly cycle divides price by 12`() {
        val monthlySub = createSubscription(price = 120_000, cycle = PaymentCycle.MONTHLY)
        val yearlySub = createSubscription(price = 120_000, cycle = PaymentCycle.YEARLY)

        val usageMillis = 30 * 3_600_000L
        val recordedDays = 30

        val monthlyResult = ValueScoreCalculator.calculate(monthlySub, usageMillis, recordedDays)
        val yearlyResult = ValueScoreCalculator.calculate(yearlySub, usageMillis, recordedDays)

        assertEquals(4000.0, monthlyResult.costPerHour!!, 0.001)
        assertEquals(333.333, yearlyResult.costPerHour!!, 0.01)
    }

    @Test
    fun `when usage time is 0, value score is LOW`() {
        val subscription = createSubscription()
        val result = ValueScoreCalculator.calculate(
            subscription = subscription,
            totalUsageTimeMillis = 0L,
            recordedDaysCount = 10
        )

        assertEquals(ValueScore.LOW, result.valueScore)
    }

    @Test
    fun `17,000 KRW subscription with 7 days recorded and 1 hour usage per day returns GOOD`() {
        val subscription = createSubscription(price = 17000, cycle = PaymentCycle.MONTHLY)
        val usageMillis = 7 * 3_600_000L
        val recordedDays = 7

        val result = ValueScoreCalculator.calculate(
            subscription = subscription,
            totalUsageTimeMillis = usageMillis,
            recordedDaysCount = recordedDays
        )

        assertEquals(ValueScore.GOOD, result.valueScore)
        assertEquals(566.666, result.costPerHour!!, 0.01)
    }
}
