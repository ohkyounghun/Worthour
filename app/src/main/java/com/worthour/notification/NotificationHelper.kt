package com.worthour.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.worthour.MainActivity
import com.worthour.R

class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val paymentChannel = NotificationChannel(
            CHANNEL_ID_PAYMENT,
            context.getString(R.string.notification_channel_payment_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_payment_desc)
        }

        val generalChannel = NotificationChannel(
            CHANNEL_ID_GENERAL,
            context.getString(R.string.notification_channel_general_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_general_desc)
        }

        notificationManager.createNotificationChannels(listOf(paymentChannel, generalChannel))
    }

    fun showPaymentNotification(
        subscriptionId: Long,
        title: String,
        message: String
    ): Boolean {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subscriptionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_PAYMENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return if (hasPermission) {
            notificationManager.notify(subscriptionId.toInt(), builder.build())
            true
        } else {
            false
        }
    }

    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL,
        notificationId: Int = 1
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, builder.build())
            }
        } else {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID_PAYMENT = "worthour_payment_channel"
        const val CHANNEL_ID_GENERAL = "worthour_general_channel"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
    }
}
