package com.worthour.ui.add

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthour.R
import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import com.worthour.data.PresetSubscription
import com.worthour.ui.home.SubscriptionLogoMapper
import com.worthour.ui.theme.WorthourPrimary
import com.worthour.ui.theme.WorthourSurface
import com.worthour.ui.theme.WorthourTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddSubscriptionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    subscriptionId: Long = 0L,
    viewModel: AddSubscriptionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(subscriptionId) {
        if (subscriptionId > 0L) {
            viewModel.initializeForEdit(subscriptionId)
        }
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onNavigateBack()
        }
    }

    AddSubscriptionContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSelectPreset = { viewModel.selectPreset(it) },
        onSelectCustom = { viewModel.selectCustomSubscription() },
        onUpdateServiceName = { viewModel.updateServiceName(it) },
        onUpdatePrice = { viewModel.updatePrice(it) },
        onUpdateCycle = { viewModel.updateCycle(it) },
        onUpdateNextPaymentDate = { viewModel.updateNextPaymentDate(it) },
        onToggleFreeTrial = { viewModel.toggleFreeTrial(it) },
        onUpdateFreeTrialEndDate = { viewModel.updateFreeTrialEndDate(it) },
        onUpdateAnalysisType = { viewModel.updateAnalysisType(it) },
        onAddLinkedPackage = { viewModel.addLinkedPackage(it) },
        onRemoveLinkedPackage = { viewModel.removeLinkedPackage(it) },
        onSaveSubscription = { viewModel.saveSubscription() },
        onDismissValidationErrorDialog = { viewModel.dismissValidationErrorDialog() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionContent(
    uiState: AddSubscriptionUiState,
    onNavigateBack: () -> Unit,
    onSelectPreset: (PresetSubscription) -> Unit,
    onSelectCustom: () -> Unit,
    onUpdateServiceName: (String) -> Unit,
    onUpdatePrice: (String) -> Unit,
    onUpdateCycle: (PaymentCycle) -> Unit,
    onUpdateNextPaymentDate: (LocalDate) -> Unit,
    onToggleFreeTrial: (Boolean) -> Unit,
    onUpdateFreeTrialEndDate: (LocalDate?) -> Unit,
    onUpdateAnalysisType: (AnalysisType) -> Unit,
    onAddLinkedPackage: (String) -> Unit,
    onRemoveLinkedPackage: (String) -> Unit,
    onSaveSubscription: () -> Unit,
    onDismissValidationErrorDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAppPickerDialog by remember { mutableStateOf(false) }
    var showDatePickerForNextBill by remember { mutableStateOf(false) }
    var showDatePickerForTrialEnd by remember { mutableStateOf(false) }

    if (uiState.showValidationErrorDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissValidationErrorDialog,
            title = { Text(stringResource(id = R.string.dialog_validation_error_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(id = R.string.dialog_validation_error_message))
                    Spacer(modifier = Modifier.height(4.dp))
                    uiState.validationErrors.forEach { err ->
                        Text("• $err", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissValidationErrorDialog) {
                    Text(stringResource(id = R.string.btn_ok), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDatePickerForNextBill) {
        val date = uiState.nextPaymentDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                onUpdateNextPaymentDate(newDate)
                showDatePickerForNextBill = false
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
        showDatePickerForNextBill = false
    }

    if (showDatePickerForTrialEnd) {
        val date = uiState.freeTrialEndDate ?: uiState.nextPaymentDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                onUpdateFreeTrialEndDate(newDate)
                showDatePickerForTrialEnd = false
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
        showDatePickerForTrialEnd = false
    }

    if (showAppPickerDialog) {
        AppSelectionDialog(
            installedApps = uiState.installedApps,
            linkedPackages = uiState.linkedPackages,
            onAddPackage = onAddLinkedPackage,
            onRemovePackage = onRemoveLinkedPackage,
            onDismiss = { showAppPickerDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) stringResource(id = R.string.edit_subscription_title) else stringResource(id = R.string.add_subscription_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Popular Presets (Only in Add mode or always visible)
            if (!uiState.isEditMode) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.popular_presets),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onSelectCustom) {
                                Text(
                                    text = stringResource(id = R.string.custom_subscription),
                                    color = WorthourPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(PresetSubscription.PRESETS) { preset ->
                                PresetChipCard(
                                    preset = preset,
                                    isSelected = uiState.selectedPresetId == preset.id,
                                    onClick = { onSelectPreset(preset) }
                                )
                            }
                        }

                        uiState.selectedPresetId?.let { selectedId ->
                            val presetName = PresetSubscription.PRESETS.find { it.id == selectedId }?.name ?: ""
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = WorthourPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.preset_selected_info, presetName),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WorthourPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Service Name Input Field
            item {
                OutlinedTextField(
                    value = uiState.serviceName,
                    onValueChange = onUpdateServiceName,
                    label = { Text(stringResource(id = R.string.service_name_label)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = uiState.serviceNameError != null,
                    supportingText = uiState.serviceNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorthourSurface,
                        unfocusedContainerColor = WorthourSurface,
                        focusedBorderColor = WorthourPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Price Input Field
            item {
                val currencyTransformation = remember { CurrencyVisualTransformation() }

                OutlinedTextField(
                    value = uiState.priceText,
                    onValueChange = onUpdatePrice,
                    label = { Text(stringResource(id = R.string.price_label)) },
                    prefix = { Text("₩ ", fontWeight = FontWeight.Bold) },
                    visualTransformation = currencyTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = uiState.priceError != null,
                    supportingText = uiState.priceError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WorthourSurface,
                        unfocusedContainerColor = WorthourSurface,
                        focusedBorderColor = WorthourPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Billing Cycle Toggle
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.billing_cycle_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(WorthourSurface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (uiState.cycle == PaymentCycle.MONTHLY) WorthourPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { onUpdateCycle(PaymentCycle.MONTHLY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (uiState.cycle == PaymentCycle.MONTHLY) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = WorthourPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(id = R.string.cycle_monthly),
                                    fontWeight = if (uiState.cycle == PaymentCycle.MONTHLY) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.cycle == PaymentCycle.MONTHLY) WorthourPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (uiState.cycle == PaymentCycle.YEARLY) WorthourPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { onUpdateCycle(PaymentCycle.YEARLY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (uiState.cycle == PaymentCycle.YEARLY) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = WorthourPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(id = R.string.cycle_yearly),
                                    fontWeight = if (uiState.cycle == PaymentCycle.YEARLY) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.cycle == PaymentCycle.YEARLY) WorthourPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. Next Billing Date Picker Card
            item {
                Card(
                    onClick = { showDatePickerForNextBill = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WorthourSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.next_billing_date_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDate(uiState.nextPaymentDate),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. Trial End Date Picker Card (Optional)
            item {
                Card(
                    onClick = {
                        if (!uiState.hasFreeTrial) {
                            onToggleFreeTrial(true)
                        } else {
                            showDatePickerForTrialEnd = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WorthourSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.trial_end_date_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.hasFreeTrial && uiState.freeTrialEndDate != null) formatDate(uiState.freeTrialEndDate) else stringResource(id = R.string.not_set),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.hasFreeTrial) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.hasFreeTrial) {
                                IconButton(onClick = { onToggleFreeTrial(false) }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Clear trial date",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 7. Analysis Method Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.analysis_method_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnalysisTypeCard(
                            title = stringResource(id = R.string.analysis_time_tracking_title),
                            description = stringResource(id = R.string.analysis_time_tracking_desc),
                            isSelected = uiState.analysisType == AnalysisType.TIME_TRACKING,
                            onClick = { onUpdateAnalysisType(AnalysisType.TIME_TRACKING) },
                            modifier = Modifier.weight(1f)
                        )
                        AnalysisTypeCard(
                            title = stringResource(id = R.string.analysis_non_time_tracking_title),
                            description = stringResource(id = R.string.analysis_non_time_tracking_desc),
                            isSelected = uiState.analysisType == AnalysisType.NON_TIME_TRACKING,
                            onClick = { onUpdateAnalysisType(AnalysisType.NON_TIME_TRACKING) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 8. Linked Apps Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WorthourSurface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.linked_apps_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.linked_apps_subtext),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (uiState.linkedPackages.isNotEmpty()) {
                            uiState.linkedPackages.forEach { pkg ->
                                val appInfo = uiState.installedApps.find { it.packageName == pkg }
                                val displayName = appInfo?.appName ?: formatFallbackAppName(pkg)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        SubscriptionServiceIcon(name = displayName)
                                        Column {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = pkg,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onRemoveLinkedPackage(pkg) }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Remove App",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showAppPickerDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, WorthourPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = WorthourPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.btn_add_app),
                                color = WorthourPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Disclaimer text
            item {
                Text(
                    text = stringResource(id = R.string.preset_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // 9. Save Subscription Primary Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onSaveSubscription,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorthourPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = if (uiState.isEditMode) stringResource(id = R.string.btn_update_subscription) else stringResource(id = R.string.btn_save_subscription),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PresetChipCard(
    preset: PresetSubscription,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(width = 100.dp, height = 110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WorthourSurface),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) WorthourPrimary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SubscriptionServiceIcon(name = preset.name)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) WorthourPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AnalysisTypeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WorthourSurface),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) WorthourPrimary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) WorthourPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun AppSelectionDialog(
    installedApps: List<InstalledAppInfo>,
    linkedPackages: List<String>,
    onAddPackage: (String) -> Unit,
    onRemovePackage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = WorthourSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.dialog_select_app_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.search_apps_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        val isLinked = linkedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isLinked) onRemovePackage(app.packageName)
                                    else onAddPackage(app.packageName)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SubscriptionServiceIcon(name = app.appName)
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isLinked) WorthourPrimary else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isLinked) WorthourPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLinked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorthourPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.btn_close), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SubscriptionServiceIcon(
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val logoResId = remember(name, isPreview) {
        if (isPreview) null else SubscriptionLogoMapper.getLogoResId(context, name)
    }

    if (logoResId != null) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = name,
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        val normalized = name.lowercase(Locale.getDefault())
        val (bgColor, textColor, label) = when {
            normalized.contains("netflix") -> Triple(Color(0xFF141414), Color(0xFFE50914), "N")
            normalized.contains("youtube") -> Triple(Color(0xFFFFEBEB), Color(0xFFFF0000), "▶")
            normalized.contains("spotify") -> Triple(Color(0xFF1DB954), Color(0xFFFFFFFF), "S")
            normalized.contains("disney") -> Triple(Color(0xFF113CCF), Color(0xFFFFFFFF), "D+")
            else -> Triple(WorthourPrimary.copy(alpha = 0.12f), WorthourPrimary, name.take(1).uppercase(Locale.getDefault()))
        }

        Box(
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length > 1) 12.sp else 16.sp
            )
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return date.format(formatter)
}

private fun formatFallbackAppName(packageName: String): String {
    val last = packageName.substringAfterLast('.')
    return last.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
}

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val number = rawText.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
        val formatted = String.format(Locale.US, "%,d", number)

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val digitsToMatch = offset.coerceAtMost(rawText.length)
                var digitsSeen = 0
                for (i in formatted.indices) {
                    if (formatted[i].isDigit()) {
                        digitsSeen++
                        if (digitsSeen == digitsToMatch) return i + 1
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val clamped = offset.coerceAtMost(formatted.length)
                var digitsSeen = 0
                for (i in 0 until clamped) {
                    if (formatted[i].isDigit()) digitsSeen++
                }
                return digitsSeen.coerceAtMost(rawText.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), numberOffsetTranslator)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AddSubscriptionPreview() {
    WorthourTheme {
        AddSubscriptionContent(
            uiState = AddSubscriptionUiState(
                selectedPresetId = "netflix",
                serviceName = "Netflix",
                priceText = "17000",
                linkedPackages = listOf("com.netflix.mediaclient")
            ),
            onNavigateBack = {},
            onSelectPreset = {},
            onSelectCustom = {},
            onUpdateServiceName = {},
            onUpdatePrice = {},
            onUpdateCycle = {},
            onUpdateNextPaymentDate = {},
            onToggleFreeTrial = {},
            onUpdateFreeTrialEndDate = {},
            onUpdateAnalysisType = {},
            onAddLinkedPackage = {},
            onRemoveLinkedPackage = {},
            onSaveSubscription = {},
            onDismissValidationErrorDialog = {}
        )
    }
}
