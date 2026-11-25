package com.worthour.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthour.R
import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import com.worthour.ui.add.SubscriptionServiceIcon
import com.worthour.ui.home.ValueScore
import com.worthour.ui.home.ValueScoreBadge
import com.worthour.ui.theme.DDayBadgeBg
import com.worthour.ui.theme.WorthourPrimary
import com.worthour.ui.theme.WorthourSurface
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    SubscriptionDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEdit = onNavigateToEdit,
        onDeleteSubscription = { viewModel.deleteSubscription(subscriptionId) },
        onDismissError = { viewModel.dismissErrorMessage() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailContent(
    uiState: SubscriptionDetailUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onDeleteSubscription: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(id = R.string.dialog_delete_title)) },
            text = { Text(stringResource(id = R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteSubscription()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }

    val errorMessage = uiState.errorMessage
    if (errorMessage != null && uiState.subscription != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text(stringResource(id = R.string.btn_ok), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        val sub = uiState.subscription

        if (sub == null && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.detail_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (sub != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header: Service Icon, Name & Subtitle
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        SubscriptionServiceIcon(name = sub.name)
                        Column {
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.detail_last_30_days_subtitle,
                                    formatDateShort(uiState.startDate30Days),
                                    formatDateShort(uiState.endDateToday)
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Top 2-Column Row: Monthly Fee & Usage (Last 30 days)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Monthly Fee Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (sub.cycle == PaymentCycle.YEARLY) stringResource(id = R.string.detail_yearly_fee_label) else stringResource(id = R.string.detail_fee_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = com.worthour.data.CurrencyConverter.formatPrice(sub.price, sub.currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Usage (Last 30 days) Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(id = R.string.detail_usage_30_days_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val usageText = if (sub.analysisType == AnalysisType.NON_TIME_TRACKING) {
                                    stringResource(id = R.string.not_set)
                                } else {
                                    formatUsageDuration(uiState.totalUsageTimeMillis30Days)
                                }
                                Text(
                                    text = usageText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Hero Card: Cost per Hour & Value Score Badge
                item {
                    val valueResult = uiState.valueResult
                    val valueScore = valueResult?.valueScore ?: ValueScore.MEASURING

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DDayBadgeBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.detail_cost_per_hour_label),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = WorthourPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ValueScoreBadge(score = valueScore)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val cph = valueResult?.costPerHour
                            val cphText = when {
                                sub.analysisType == AnalysisType.NON_TIME_TRACKING -> stringResource(id = R.string.score_not_tracked)
                                valueScore == ValueScore.MEASURING -> stringResource(id = R.string.score_measuring)
                                cph == null || cph.isInfinite() || cph.isNaN() -> stringResource(id = R.string.insights_no_usage)
                                else -> stringResource(id = R.string.insights_per_hour_format, String.format(Locale.US, "%,d", kotlin.math.round(cph).toLong()))
                            }

                            Text(
                                text = cphText,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = if (cphText.length > 10) 24.sp else 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WorthourPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val formulaText = when {
                                sub.analysisType == AnalysisType.NON_TIME_TRACKING -> stringResource(id = R.string.detail_formula_not_tracked)
                                valueScore == ValueScore.MEASURING -> stringResource(id = R.string.detail_formula_measuring)
                                uiState.totalUsageTimeMillis30Days == 0L || cph == null || cph.isInfinite() || cph.isNaN() -> stringResource(id = R.string.detail_formula_no_usage)
                                else -> {
                                    stringResource(
                                        id = R.string.detail_formula_format,
                                        com.worthour.data.CurrencyConverter.formatPrice(sub.price, sub.currency),
                                        formatUsageDuration(uiState.totalUsageTimeMillis30Days)
                                    )
                                }
                            }

                            Text(
                                text = formulaText,
                                style = MaterialTheme.typography.bodySmall,
                                color = WorthourPrimary.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 4. Daily Usage Section Card (Hidden for NON_TIME_TRACKING)
                if (sub.analysisType != AnalysisType.NON_TIME_TRACKING) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.detail_daily_usage_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val totalMin30 = (uiState.totalUsageTimeMillis30Days / 1000) / 60
                                    Text(
                                        text = stringResource(id = R.string.detail_total_minutes_format, totalMin30),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (uiState.dailyUsages.isEmpty()) {
                                    Text(
                                        text = stringResource(id = R.string.detail_no_data),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    uiState.dailyUsages.forEach { daily ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = formatDateShort(daily.date),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            val displayMinutes = when {
                                                !daily.hasRecord -> stringResource(id = R.string.detail_no_data)
                                                daily.usageTimeMillis <= 0L -> "0m"
                                                daily.usageTimeMillis < 60_000L -> "<1m"
                                                else -> formatUsageDuration(daily.usageTimeMillis)
                                            }
                                            Text(
                                                text = displayMinutes,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (daily.hasRecord && daily.usageTimeMillis > 0) WorthourPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Next Billing Date Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = WorthourPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.detail_next_billing_date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatDateFull(sub.nextPaymentDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 6. Free Trial End Date Card (if active)
                if (sub.freeTrialEndDate != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = WorthourPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.detail_trial_ends_date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatDateFull(sub.freeTrialEndDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WorthourPrimary
                                )
                            }
                        }
                    }
                }

                // 7. Linked Apps Section Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Linked Apps (${uiState.linkedApps.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.linkedApps.isEmpty()) {
                                Text(
                                    text = stringResource(id = R.string.detail_no_apps_linked),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.linkedApps.forEach { app ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        SubscriptionServiceIcon(name = app.packageName)
                                        Column {
                                            Text(
                                                text = app.packageName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 8. Disclaimer Caption
                item {
                    Text(
                        text = stringResource(id = R.string.detail_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // 9. Edit & Delete Action Buttons
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onNavigateToEdit(sub.id) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WorthourPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = stringResource(id = R.string.btn_edit_subscription),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = stringResource(id = R.string.btn_delete_subscription),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private fun formatUsageDuration(totalTimeMillis: Long): String {
    val totalMinutes = (totalTimeMillis / 1000) / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun formatDateShort(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    return date.format(formatter)
}

private fun formatDateFull(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return date.format(formatter)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SubscriptionDetailPreview() {
    com.worthour.ui.theme.WorthourTheme {
        SubscriptionDetailContent(
            uiState = SubscriptionDetailUiState(
                subscription = com.worthour.data.SubscriptionEntity(
                    id = 1,
                    name = "Netflix",
                    price = 17000,
                    nextPaymentDate = LocalDate.of(2026, 10, 11),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueResult = com.worthour.data.ValueCalculationResult(
                    costPerHour = 8500.0,
                    valueScore = ValueScore.LOW
                ),
                linkedApps = listOf(
                    com.worthour.data.AppEntity(id = 1, subscriptionId = 1, packageName = "com.netflix.mediaclient")
                ),
                dailyUsages = listOf(
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 8), usageTimeMillis = 30 * 60 * 1000L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 7), usageTimeMillis = 10 * 60 * 1000L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 6), usageTimeMillis = 15 * 60 * 1000L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 5), usageTimeMillis = 35 * 60 * 1000L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 4), usageTimeMillis = 0L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 3), usageTimeMillis = 20 * 60 * 1000L, hasRecord = true),
                    DailyUsageUiModel(date = LocalDate.of(2026, 10, 2), usageTimeMillis = 10 * 60 * 1000L, hasRecord = true)
                ),
                totalUsageTimeMillis30Days = 120 * 60 * 1000L,
                recordedDaysCount = 30,
                isLoading = false
            ),
            onNavigateBack = {},
            onNavigateToEdit = {},
            onDeleteSubscription = {},
            onDismissError = {}
        )
    }
}
