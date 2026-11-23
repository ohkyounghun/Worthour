package com.worthour.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class SubscriptionJsonConverterTest {

    @Test
    fun serialize_and_deserialize_list_of_subscriptions_with_linked_apps_and_free_trial_date() {
        val sampleList = listOf(
            ShareableSubscription(
                name = "Netflix",
                price = 17000,
                nextPaymentDate = LocalDate.of(2026, 12, 1),
                freeTrialEndDate = LocalDate.of(2026, 11, 30),
                cycle = PaymentCycle.MONTHLY,
                analysisType = AnalysisType.TIME_TRACKING,
                linkedAppPackageNames = listOf("com.netflix.mediaclient")
            ),
            ShareableSubscription(
                name = "Gym Membership",
                price = 120000,
                nextPaymentDate = LocalDate.of(2027, 1, 1),
                freeTrialEndDate = null,
                cycle = PaymentCycle.YEARLY,
                analysisType = AnalysisType.NON_TIME_TRACKING,
                linkedAppPackageNames = emptyList()
            )
        )

        val json = SubscriptionJsonConverter.toJson(sampleList)

        // Verify JSON string format (id should not be in json)
        assertFalse(json.contains("\"id\""))
        assertTrue(json.contains("Netflix"))
        assertTrue(json.contains("com.netflix.mediaclient"))
        assertTrue(json.contains("Gym Membership"))

        // Deserialize back
        val deserializedList = SubscriptionJsonConverter.listFromJson(json)

        assertEquals(2, deserializedList.size)

        val item1 = deserializedList[0]
        assertEquals("Netflix", item1.name)
        assertEquals(17000, item1.price)
        assertEquals(LocalDate.of(2026, 12, 1), item1.nextPaymentDate)
        assertEquals(LocalDate.of(2026, 11, 30), item1.freeTrialEndDate)
        assertEquals(PaymentCycle.MONTHLY, item1.cycle)
        assertEquals(AnalysisType.TIME_TRACKING, item1.analysisType)
        assertEquals(listOf("com.netflix.mediaclient"), item1.linkedAppPackageNames)

        val item2 = deserializedList[1]
        assertEquals("Gym Membership", item2.name)
        assertEquals(120000, item2.price)
        assertEquals(LocalDate.of(2027, 1, 1), item2.nextPaymentDate)
        assertNull(item2.freeTrialEndDate)
        assertEquals(PaymentCycle.YEARLY, item2.cycle)
        assertEquals(AnalysisType.NON_TIME_TRACKING, item2.analysisType)
        assertTrue(item2.linkedAppPackageNames.isEmpty())
    }

    @Test
    fun convert_shareable_subscription_to_subscription_entity() {
        val shareable = ShareableSubscription(
            name = "YouTube Premium",
            price = 14900,
            nextPaymentDate = LocalDate.of(2026, 12, 15),
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING,
            linkedAppPackageNames = listOf("com.google.android.youtube")
        )

        val entity = shareable.toSubscriptionEntity(id = 42L)

        assertEquals(42L, entity.id)
        assertEquals("YouTube Premium", entity.name)
        assertEquals(14900, entity.price)
        assertEquals(LocalDate.of(2026, 12, 15), entity.nextPaymentDate)
        assertNull(entity.freeTrialEndDate)
        assertEquals(PaymentCycle.MONTHLY, entity.cycle)
        assertEquals(AnalysisType.TIME_TRACKING, entity.analysisType)
    }
}
