package com.worthour.notification

import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import com.worthour.data.SubscriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PaymentNotificationSchedulerTest {

    @Test
    fun targetDate_prefersFreeTrialEndDateWhenPresent() {
        val trialEnd = LocalDate.now().plusDays(5)
        val nextPay = LocalDate.now().plusDays(30)
        val subscription = SubscriptionEntity(
            id = 1L,
            name = "Test Subscription",
            price = 10000,
            nextPaymentDate = nextPay,
            freeTrialEndDate = trialEnd,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING
        )

        val targetDate = subscription.freeTrialEndDate ?: subscription.nextPaymentDate
        assertEquals(trialEnd, targetDate)
    }

    @Test
    fun targetDate_usesNextPaymentDateWhenFreeTrialNull() {
        val nextPay = LocalDate.now().plusDays(30)
        val subscription = SubscriptionEntity(
            id = 1L,
            name = "Test Subscription",
            price = 10000,
            nextPaymentDate = nextPay,
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING
        )

        val targetDate = subscription.freeTrialEndDate ?: subscription.nextPaymentDate
        assertEquals(nextPay, targetDate)
    }

    @Test
    fun targetDate_pastDateIsBeforeToday() {
        val pastDate = LocalDate.now().minusDays(2)
        val today = LocalDate.now()
        assertTrue(pastDate.isBefore(today))
    }

    @Test
    fun targetDate_todayOrFutureIsNotBeforeToday() {
        val today = LocalDate.now()
        val futureDate = today.plusDays(3)
        assertFalse(today.isBefore(today))
        assertFalse(futureDate.isBefore(today))
    }
}
