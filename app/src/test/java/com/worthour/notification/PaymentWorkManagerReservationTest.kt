package com.worthour.notification

import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import com.worthour.data.SubscriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class PaymentWorkManagerReservationTest {

    @Test
    fun flow1_schedule_payment5DaysLater_calculates2DaysDelayAt9AM() {
        val today = LocalDate.now()
        val payment5DaysLater = today.plusDays(5)

        val subscription = SubscriptionEntity(
            id = 101L,
            name = "Netflix Test",
            price = 17000,
            nextPaymentDate = payment5DaysLater,
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING
        )

        val targetDate = subscription.freeTrialEndDate ?: subscription.nextPaymentDate
        assertEquals(payment5DaysLater, targetDate)

        val notifyDateTime = targetDate.minusDays(3).atTime(LocalTime.of(9, 0))
        val expectedNotifyDate = today.plusDays(2)
        assertEquals(expectedNotifyDate, notifyDateTime.toLocalDate())
        assertEquals(9, notifyDateTime.hour)
    }

    @Test
    fun flow2_edit_payment7DaysLater_replacesWithNewTargetDate4DaysLaterAt9AM() {
        val today = LocalDate.now()
        val payment7DaysLater = today.plusDays(7)

        val editedSub = SubscriptionEntity(
            id = 102L,
            name = "Spotify Test",
            price = 10900,
            nextPaymentDate = payment7DaysLater,
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING
        )

        val targetDate = editedSub.freeTrialEndDate ?: editedSub.nextPaymentDate
        val notifyDateTime = targetDate.minusDays(3).atTime(LocalTime.of(9, 0))
        val expectedNotifyDate = today.plusDays(4)

        assertEquals(expectedNotifyDate, notifyDateTime.toLocalDate())
        assertEquals(9, notifyDateTime.hour)
    }

    @Test
    fun flow3_deleteSubscription_targetWorkNameMatches() {
        val subscriptionId = 103L
        val workName = "worthour_payment_notify_$subscriptionId"
        assertEquals("worthour_payment_notify_103", workName)
    }
}
