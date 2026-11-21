package com.worthour.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.worthour.R
import com.worthour.data.AppDatabase
import com.worthour.data.SubscriptionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

class PaymentNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val reviewNoteProvider: PaymentReviewNoteProvider = DefaultPaymentReviewNoteProvider()

    override suspend fun doWork(): Result {
        val subscriptionId = inputData.getLong(EXTRA_SUBSCRIPTION_ID, -1L)
        val targetDateStr = inputData.getString(EXTRA_TARGET_DATE)
        val isDebugTest = inputData.getBoolean(EXTRA_IS_DEBUG_TEST, false)

        if (subscriptionId <= 0L) return Result.success()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SubscriptionRepository(database.subscriptionDao())
        val subscription = repository.getSubscriptionById(subscriptionId).firstOrNull() ?: return Result.success()

        val currentTargetDateStr = (subscription.freeTrialEndDate ?: subscription.nextPaymentDate).toString()

        // If not debug test, verify that the scheduled date matches current subscription date
        if (!isDebugTest && targetDateStr != null && currentTargetDateStr != targetDateStr) {
            return Result.success()
        }

        // Duplicate check (skip if already sent for this payment date)
        if (!isDebugTest && targetDateStr != null) {
            val alreadySent = PaymentNotificationPrefs.isNotificationSent(applicationContext, subscriptionId, targetDateStr)
            if (alreadySent) return Result.success()
        }

        val reviewNote = reviewNoteProvider.getReviewNote(applicationContext, subscription)

        val isTrial = subscription.freeTrialEndDate != null
        val title = if (isTrial) {
            applicationContext.getString(R.string.notification_payment_trial_title, subscription.name)
        } else {
            applicationContext.getString(R.string.notification_payment_title, subscription.name)
        }

        val formattedPrice = String.format(Locale.US, "%,d", subscription.price)
        val formattedDate = (subscription.freeTrialEndDate ?: subscription.nextPaymentDate).toString()

        val bodyText = if (isTrial) {
            applicationContext.getString(R.string.notification_payment_trial_body, subscription.name, formattedDate)
        } else {
            applicationContext.getString(R.string.notification_payment_body, subscription.name, formattedPrice, formattedDate)
        }

        val finalBody = if (!reviewNote.isNullOrBlank()) {
            "$bodyText\n\n💡 $reviewNote"
        } else {
            bodyText
        }

        val notificationHelper = NotificationHelper(applicationContext)
        val isPosted = notificationHelper.showPaymentNotification(subscription.id, title, finalBody)

        if (isPosted && !isDebugTest && targetDateStr != null) {
            PaymentNotificationPrefs.markNotificationSent(applicationContext, subscription.id, targetDateStr)
        }

        return Result.success()
    }

    companion object {
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_TARGET_DATE = "extra_target_date"
        const val EXTRA_IS_DEBUG_TEST = "extra_is_debug_test"
    }
}
