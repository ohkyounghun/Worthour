package com.worthour.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> {
        return subscriptionDao.getAllSubscriptions()
    }

    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?> {
        return subscriptionDao.getSubscriptionById(id)
    }

    fun getLinkedApps(subId: Long): Flow<List<AppEntity>> {
        return subscriptionDao.getLinkedApps(subId)
    }

    suspend fun saveSubscriptionWithApps(
        subscription: SubscriptionEntity,
        packageNames: List<String>
    ): Long {
        val subId = if (subscription.id == 0L) {
            subscriptionDao.insertSubscription(subscription)
        } else {
            subscriptionDao.updateSubscription(subscription)
            subscription.id
        }
        subscriptionDao.deleteLinkedApps(subId)
        packageNames.distinct().forEach { pkg ->
            if (pkg.isNotBlank()) {
                subscriptionDao.insertApp(AppEntity(subscriptionId = subId, packageName = pkg))
            }
        }
        return subId
    }

    suspend fun deleteSubscriptionById(id: Long) {
        subscriptionDao.deleteLinkedApps(id)
        subscriptionDao.deleteSubscriptionById(id)
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
