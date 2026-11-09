package com.worthour.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Insert
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Insert
    suspend fun insertApp(app: AppEntity): Long

    @Query("DELETE FROM linked_apps WHERE subscriptionId = :subId")
    suspend fun deleteLinkedApps(subId: Long)

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?>

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Long)

    @Query("SELECT * FROM linked_apps WHERE subscriptionId = :subId")
    fun getLinkedApps(subId: Long): Flow<List<AppEntity>>
}
