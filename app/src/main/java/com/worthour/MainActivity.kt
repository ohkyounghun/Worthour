package com.worthour

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.worthour.ui.theme.WorthourTheme
import com.worthour.usage.UsageDebugReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorthourTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        UsageTestButton()
                    }
                }
            }
        }
    }
}

@Composable
fun UsageTestButton() {
    val context = LocalContext.current
    Button(onClick = {
        val reader = UsageDebugReader(context)
        if (reader.hasUsageStatsPermission()) {
            reader.printUsageStats()
        } else {
            Log.d("USAGE", "권한 없음")
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }
    }) {
        Text("사용량 테스트")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WorthourTheme {
        UsageTestButton()
    }
}