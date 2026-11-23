package com.worthour.data

import com.worthour.ui.home.ValueScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GetSubscriptionValueUseCaseTest {

    private class FakeSubscriptionDao(
        private val subscriptions: List<SubscriptionEntity> = emptyList(),
        private val linkedAppsMap: Map<Long, List<AppEntity>> = emptyMap()
    ) : SubscriptionDao {
        override suspend fun insertSubscription(subscription: SubscriptionEntity) = 1L
        override suspend fun updateSubscription(subscription: SubscriptionEntity) {}
        override suspend fun deleteSubscription(subscription: SubscriptionEntity) {}
        override suspend fun insertApp(app: AppEntity) = 1L
        override suspend fun deleteLinkedApps(subId: Long) {}
        override fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> = flowOf(subscriptions)
        override fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?> = flowOf(subscriptions.firstOrNull { it.id == id })
        override suspend fun deleteSubscriptionById(id: Long) {}
        override suspend fun deleteAllSubscriptions() {}
        override suspend fun deleteAllLinkedApps() {}
        override fun getLinkedApps(subId: Long): Flow<List<AppEntity>> = flowOf(linkedAppsMap[subId] ?: emptyList())
    }

    private class FakeUsageRecordDao(
        private val packageUsageMap: Map<List<String>, Long> = emptyMap(),
        private val recordedDaysCount: Int = 0
    ) : UsageRecordDao {
        override suspend fun insertUsageRecord(record: UsageRecordEntity) = 1L
        override suspend fun getUsageRecord(packageName: String, date: LocalDate): UsageRecordEntity? = null
        override fun getUsageRecords(packageName: String, startDate: LocalDate, endDate: LocalDate) = flowOf(emptyList<UsageRecordEntity>())
        override suspend fun getTotalUsageTimeForPackages(packageNames: List<String>, startDate: LocalDate) = packageUsageMap[packageNames] ?: 0L
        override fun getTotalUsageTimeForPackagesFlow(packageNames: List<String>, startDate: LocalDate) = flowOf(packageUsageMap[packageNames] ?: 0L)
        override suspend fun getRecordedDaysCount(startDate: LocalDate) = recordedDaysCount
        override fun getRecordedDaysCountFlow(startDate: LocalDate) = flowOf(recordedDaysCount)
    }

    @Test
    fun `non time tracking subscription returns NOT_TRACKED`() = runBlocking {
        val subscription = SubscriptionEntity(
            id = 1L,
            name = "Non Time Tracking Sub",
            price = 10000,
            nextPaymentDate = LocalDate.now(),
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.NON_TIME_TRACKING
        )

        val subRepo = SubscriptionRepository(FakeSubscriptionDao(listOf(subscription)))
        val usageRepo = UsageRepository(FakeUsageRecordDao())
        val useCase = GetSubscriptionValueUseCase(subRepo, usageRepo)

        val result = useCase.getSubscriptionValueFlow(subscription).first()

        assertEquals(ValueScore.NOT_TRACKED, result.valueScore)
        assertNull(result.costPerHour)
    }

    @Test
    fun `time tracking subscription with enough recorded days returns calculated score in list`() = runBlocking {
        val subscription = SubscriptionEntity(
            id = 2L,
            name = "Netflix",
            price = 17000,
            nextPaymentDate = LocalDate.now(),
            freeTrialEndDate = null,
            cycle = PaymentCycle.MONTHLY,
            analysisType = AnalysisType.TIME_TRACKING
        )

        val linkedApps = listOf(AppEntity(id = 1, subscriptionId = 2L, packageName = "com.netflix.mediaclient"))
        val subDao = FakeSubscriptionDao(
            subscriptions = listOf(subscription),
            linkedAppsMap = mapOf(2L to linkedApps)
        )

        val usageDao = FakeUsageRecordDao(
            packageUsageMap = mapOf(listOf("com.netflix.mediaclient") to 7 * 3_600_000L),
            recordedDaysCount = 7
        )

        val subRepo = SubscriptionRepository(subDao)
        val usageRepo = UsageRepository(usageDao)
        val useCase = GetSubscriptionValueUseCase(subRepo, usageRepo)

        val resultList = useCase.getAllSubscriptionValuesFlow(flowOf(LocalDate.now())).first()

        assertEquals(1, resultList.size)
        val item = resultList[0]
        assertEquals(2L, item.subscription.id)
        assertEquals(ValueScore.GOOD, item.valueScore)
        assertEquals(566.666, item.costPerHour!!, 0.01)
    }
}
