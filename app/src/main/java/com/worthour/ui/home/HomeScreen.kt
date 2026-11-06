package com.worthour.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text("Home Screen")
        Button(onClick = onNavigateToAdd) {
            Text("Go to Add Subscription")
        }
        Button(onClick = { onNavigateToDetail(1L) }) {
            Text("Go to Subscription Detail (ID 1)")
        }
        Button(onClick = onNavigateToInsights) {
            Text("Go to Insights")
        }
        Button(onClick = onNavigateToNotifications) {
            Text("Go to Notifications")
        }
        Button(onClick = onNavigateToPermission) {
            Text("Go to Permission")
        }
    }
}
