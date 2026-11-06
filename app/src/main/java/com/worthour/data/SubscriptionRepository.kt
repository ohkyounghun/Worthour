package com.worthour.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getAllSubscriptions()
    }

    fun getLinkedApps(subId: Long): Flow<List<AppEntity>> {
        return subscriptionDao.getLinkedApps(subId)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity): Long {
        return subscriptionDao.insertSubscription(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun insertApp(app: AppEntity): Long {
        return subscriptionDao.insertApp(app)
    }

    suspend fun deleteLinkedApps(subId: Long) {
        subscriptionDao.deleteLinkedApps(subId)
    }
}
