package com.worthour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.worthour.ui.navigation.WorthourNavGraph
import com.worthour.ui.theme.WorthourTheme
import com.worthour.usage.UsageSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Daily WorkManager sync scheduling & initial 7-day sync
        UsageSyncManager.scheduleDailySync(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            UsageSyncManager.syncInitialLast7DaysIfNeeded(applicationContext)
        }

        setContent {
            WorthourTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WorthourNavGraph(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}