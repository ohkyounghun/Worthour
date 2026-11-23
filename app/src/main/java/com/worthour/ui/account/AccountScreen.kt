package com.worthour.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthour.R
import com.worthour.ui.theme.WorthourPrimary
import com.worthour.ui.theme.WorthourSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestoreDialog by rememberSaveable { mutableStateOf(false) }

    val emptyFieldsMsg = stringResource(id = R.string.account_error_empty_fields)
    val loginSuccessMsg = stringResource(id = R.string.account_msg_login_success)
    val signupSuccessMsg = stringResource(id = R.string.account_msg_signup_success)
    val logoutSuccessMsg = stringResource(id = R.string.account_msg_logout_success)
    val backupSuccessMsg = stringResource(id = R.string.account_msg_backup_success)
    val restoreSuccessMsg = stringResource(id = R.string.account_msg_restore_success)
    val noBackupFoundMsg = stringResource(id = R.string.account_msg_no_backup_found)

    LaunchedEffect(uiState.authMessage) {
        val msg = when (val message = uiState.authMessage) {
            AccountAuthMessage.EmptyFields -> emptyFieldsMsg
            AccountAuthMessage.LoginSuccess -> loginSuccessMsg
            AccountAuthMessage.SignupSuccess -> signupSuccessMsg
            AccountAuthMessage.LogoutSuccess -> logoutSuccessMsg
            AccountAuthMessage.BackupSuccess -> backupSuccessMsg
            AccountAuthMessage.RestoreSuccess -> restoreSuccessMsg
            AccountAuthMessage.NoBackupFound -> noBackupFoundMsg
            is AccountAuthMessage.AuthFailed -> message.errorMessage
            null -> null
        }
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAuthMessage()
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.account_dialog_restore_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(id = R.string.account_dialog_restore_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        viewModel.restoreSubscriptions()
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.account_btn_restore_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(text = stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.account_title),
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WorthourSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = WorthourPrimary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val statusText = if (uiState.currentUser != null) {
                            stringResource(
                                id = R.string.account_status_logged_in,
                                uiState.currentUser?.email ?: ""
                            )
                        } else {
                            stringResource(id = R.string.account_status_guest)
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (uiState.currentUser != null) {
                // Logged in state -> Sign Out Button
                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(id = R.string.account_btn_logout))
                }
            } else {
                // Guest state -> Guest notice & Login / Signup Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WorthourSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.account_guest_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = viewModel::onEmailInputChanged,
                    label = { Text(stringResource(id = R.string.account_email_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.passwordInput,
                    onValueChange = viewModel::onPasswordInputChanged,
                    label = { Text(stringResource(id = R.string.account_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.signIn() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.account_btn_login))
                        }

                        OutlinedButton(
                            onClick = { viewModel.signUp() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.account_btn_signup))
                        }
                    }
                }
            }

            // Cloud Backup & Restore Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WorthourSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.account_section_backup_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isUserLoggedIn = uiState.currentUser != null
                        val isBtnEnabled = isUserLoggedIn && !uiState.isLoading

                        Button(
                            onClick = { viewModel.backupSubscriptions() },
                            enabled = isBtnEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.account_btn_backup))
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            enabled = isBtnEnabled,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(id = R.string.account_btn_restore))
                        }
                    }
                }
            }
        }
    }
}
