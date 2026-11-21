package com.worthour.notification

import android.content.Context
import androidx.core.content.edit

object PaymentNotificationPrefs {
    private const val PREFS_NAME = "worthour_payment_notification_prefs"

    private fun keyFor(subscriptionId: Long, targetDateStr: String): String {
        return "sent_${subscriptionId}_$targetDateStr"
    }

    fun isNotificationSent(context: Context, subscriptionId: Long, targetDateStr: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(keyFor(subscriptionId, targetDateStr), false)
    }

    fun markNotificationSent(context: Context, subscriptionId: Long, targetDateStr: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(keyFor(subscriptionId, targetDateStr), true)
        }
    }
}
