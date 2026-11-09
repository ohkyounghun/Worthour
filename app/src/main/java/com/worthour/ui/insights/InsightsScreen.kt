package com.worthour.ui.insights

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.worthour.ui.home.WorthourBottomNavigationBar
import com.worthour.ui.navigation.Screen

@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            WorthourBottomNavigationBar(
                currentScreen = Screen.INSIGHTS,
                onNavigateToHome = onNavigateBack,
                onNavigateToInsights = { /* Already on Insights */ }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Insights Screen",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
