package com.worthour.notification

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.worthour.data.AppDatabase
import com.worthour.data.SubscriptionEntity
import com.worthour.data.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object PaymentNotificationScheduler {

    private const val NOTIFY_DAYS_BEFORE = 3L
    private val NOTIFY_TIME = LocalTime.of(9, 0) // 9:00 AM local device time

    private fun getUniqueWorkName(subscriptionId: Long): String {
        return "worthour_payment_notify_$subscriptionId"
    }

    /**
     * Schedules a payment notification 3 days before payment at 9:00 AM.
     * Uses freeTrialEndDate as target date if present, else nextPaymentDate.
     */
    fun scheduleNotification(context: Context, subscription: SubscriptionEntity) {
        val targetDate = subscription.freeTrialEndDate ?: subscription.nextPaymentDate
        val targetDateStr = targetDate.toString()
        val today = LocalDate.now()

        // Past payment date: do not notify
        if (targetDate.isBefore(today)) {
            cancelNotification(context, subscription.id)
            return
        }

        val notifyDateTime = targetDate.minusDays(NOTIFY_DAYS_BEFORE).atTime(NOTIFY_TIME)
        val now = LocalDateTime.now()

        val delayMillis = if (now.isBefore(notifyDateTime)) {
            Duration.between(now, notifyDateTime).toMillis()
        } else {
            // Scheduled notify time has passed, but payment date is today or future.
            // Notify once if not already sent for this target date.
            val alreadySent = PaymentNotificationPrefs.isNotificationSent(context, subscription.id, targetDateStr)
            if (alreadySent) {
                return
            }
            0L
        }

        val inputData = Data.Builder()
            .putLong(PaymentNotificationWorker.EXTRA_SUBSCRIPTION_ID, subscription.id)
            .putString(PaymentNotificationWorker.EXTRA_TARGET_DATE, targetDateStr)
            .putBoolean(PaymentNotificationWorker.EXTRA_IS_DEBUG_TEST, false)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PaymentNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("tag_payment_notification")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            getUniqueWorkName(subscription.id),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Reschedules all valid pending notifications for all active subscriptions.
     * Useful when the user returns to the app after granting notification permission in Settings.
     */
    fun rescheduleAllPendingNotifications(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val repository = SubscriptionRepository(database.subscriptionDao())
            val subs = repository.getAllSubscriptions().firstOrNull() ?: emptyList()
            val today = LocalDate.now()
            subs.forEach { sub ->
                val targetDate = sub.freeTrialEndDate ?: sub.nextPaymentDate
                if (!targetDate.isBefore(today)) {
                    scheduleNotification(context, sub)
                }
            }
        }
    }

    /**
     * Cancels scheduled notification for the subscription ID.
     */
    fun cancelNotification(context: Context, subscriptionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(getUniqueWorkName(subscriptionId))
    }

    /**
     * Debug-only test notification trigger.
     * Triggers a notification 3 seconds later without modifying real schedules or DB sent states.
     */
    fun scheduleDebugTestNotification(context: Context, subscriptionId: Long) {
        val inputData = Data.Builder()
            .putLong(PaymentNotificationWorker.EXTRA_SUBSCRIPTION_ID, subscriptionId)
            .putBoolean(PaymentNotificationWorker.EXTRA_IS_DEBUG_TEST, true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PaymentNotificationWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS)
            .setInputData(inputData)
            .addTag("tag_debug_notification")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "worthour_payment_notify_debug_$subscriptionId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
