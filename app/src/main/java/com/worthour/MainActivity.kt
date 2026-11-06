package com.worthour

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.worthour.ui.navigation.WorthourNavGraph
import com.worthour.ui.theme.WorthourTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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