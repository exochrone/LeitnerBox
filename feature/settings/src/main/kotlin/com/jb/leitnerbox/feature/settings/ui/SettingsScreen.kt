package com.jb.leitnerbox.feature.settings.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.backup.BackupSummary
import com.jb.leitnerbox.feature.settings.BuildConfig
import com.jb.leitnerbox.feature.settings.R
import com.jb.leitnerbox.feature.settings.ui.debug.DebugSection
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel(),
    onRestoreSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupUiState by backupViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver
                .openInputStream(it)
                ?.use { stream -> stream.readBytes() }
                ?: return@let
            backupViewModel.onFileSelected(bytes)
        }
    }

    LaunchedEffect(Unit) {
        backupViewModel.events.collect { event ->
            when (event) {
                is BackupEvent.ShareFile -> {
                    val fileName = "LeitnerBox_sauvegarde_${LocalDate.now()}.leitnerbackup"
                    val file = File(context.cacheDir, fileName)
                    file.writeBytes(event.bytes)
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/octet-stream"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                }
                BackupEvent.RestoreSuccess -> {
                    onRestoreSuccess()
                }
            }
        }
    }

    if (showTimePicker) {
        val time = uiState.notificationTime
        TimePickerDialog(
            context,
            { _, hour, minute -> 
                viewModel.onNotificationTimeSelected(hour, minute)
                showTimePicker = false
            },
            time.hour,
            time.minute,
            DateFormat.is24HourFormat(context)
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    if (backupUiState.showConfirmDialog && backupUiState.backupSummary != null) {
        RestoreConfirmDialog(
            summary = backupUiState.backupSummary!!,
            onConfirm = backupViewModel::onRestoreConfirmed,
            onCancel = backupViewModel::onRestoreCancelled
        )
    }

    backupUiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = backupViewModel::onErrorDismissed,
            title = { Text(stringResource(R.string.backup_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = backupViewModel::onErrorDismissed) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsContent(
                uiState = uiState,
                onDayToggled = viewModel::onDayToggled,
                onThemeSelected = viewModel::onThemeSelected,
                onNotificationTimeClick = { showTimePicker = true },
                backupSection = {
                    BackupSection(
                        isLoading = backupUiState.isLoading,
                        onExport = backupViewModel::onExportClick,
                        onImport = { openFileLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                    )
                },
                debugSection = {
                    if (BuildConfig.DEBUG) {
                        DebugSection()
                    }
                }
            )
        }
    }
}

@Composable
private fun RestoreConfirmDialog(
    summary: BackupSummary,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(R.string.backup_restore_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.backup_restore_warning))
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.backup_restore_deck_count, summary.deckCount))
                Text(stringResource(R.string.backup_restore_card_count, summary.cardCount))
                Text(stringResource(R.string.backup_restore_session_count, summary.sessionCount))
                Text(stringResource(R.string.backup_restore_date, summary.exportDate))
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.backup_restore_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
