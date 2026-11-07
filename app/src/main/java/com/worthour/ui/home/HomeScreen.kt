package com.worthour.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthour.R
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val topApps by viewModel.topApps.collectAsStateWithLifecycle()
    val permissionWarning by viewModel.permissionWarning.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text("Home Screen", style = MaterialTheme.typography.headlineMedium)

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

        Spacer(modifier = Modifier.height(16.dp))

        // 임시 사용량 테스트 버튼
        Button(onClick = { viewModel.loadTodayTopApps() }) {
            Text(stringResource(id = R.string.home_test_usage_button))
        }

        if (permissionWarning) {
            Text(
                text = stringResource(id = R.string.home_permission_warning),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (topApps.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.home_top_apps_title),
                style = MaterialTheme.typography.titleMedium
            )
            topApps.forEach { app ->
                val minutes = app.usageTimeMillis / 1000 / 60
                val seconds = (app.usageTimeMillis / 1000) % 60
                val timeStr = String.format(Locale.US, "%d분 %d초", minutes, seconds)
                Text(
                    text = "${app.packageName}: $timeStr",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
