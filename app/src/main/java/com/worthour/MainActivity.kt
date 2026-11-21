package com.worthour

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.worthour.notification.NotificationHelper
import com.worthour.notification.PaymentNotificationScheduler
import com.worthour.ui.navigation.WorthourNavGraph
import com.worthour.ui.theme.WorthourTheme
import com.worthour.usage.UsageSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val deepLinkSubIdState = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleDeepLinkIntent(intent)

        // Daily WorkManager sync scheduling & initial 7-day sync
        UsageSyncManager.scheduleDailySync(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            UsageSyncManager.syncInitialLast7DaysIfNeeded(applicationContext)
        }

        setContent {
            WorthourTheme {
                val deepLinkId by deepLinkSubIdState
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WorthourNavGraph(
                        deepLinkSubscriptionId = deepLinkId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PaymentNotificationScheduler.rescheduleAllPendingNotifications(applicationContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)) {
            val subId = intent.getLongExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID, -1L)
            if (subId > 0L) {
                deepLinkSubIdState.value = subId
            }
        }
    }
}
