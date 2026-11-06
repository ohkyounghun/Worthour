package com.worthour.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
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

    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_GENERAL,
        notificationId: Int = 1
    ) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a proper icon later
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
    }
}
