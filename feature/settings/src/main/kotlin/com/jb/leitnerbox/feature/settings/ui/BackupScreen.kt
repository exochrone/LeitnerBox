package com.jb.leitnerbox.feature.settings.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.backup.BackupSummary
import com.jb.leitnerbox.feature.settings.R
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateToCsvExport: () -> Unit,
    onNavigateToCsvImport: () -> Unit,
    onBackClick: () -> Unit,
    onRestoreSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver
                .openInputStream(it)
                ?.use { stream -> stream.readBytes() }
                ?: return@let
            viewModel.onFileSelected(bytes)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
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

    if (uiState.showConfirmDialog && uiState.backupSummary != null) {
        RestoreConfirmDialog(
            summary = uiState.backupSummary!!,
            onConfirm = viewModel::onRestoreConfirmed,
            onCancel = viewModel::onRestoreCancelled
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::onErrorDismissed,
            title = { Text(stringResource(R.string.backup_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::onErrorDismissed) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_backup_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                text = stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = viewModel::onExportClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.backup_export_button))
            }

            OutlinedButton(
                onClick = {
                    openFileLauncher.launch(
                        arrayOf("application/octet-stream", "*/*")
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.backup_import_button))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.csv_section_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            OutlinedButton(
                onClick = onNavigateToCsvExport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.csv_export_button))
            }

            OutlinedButton(
                onClick = onNavigateToCsvImport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.csv_import_button))
            }
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
