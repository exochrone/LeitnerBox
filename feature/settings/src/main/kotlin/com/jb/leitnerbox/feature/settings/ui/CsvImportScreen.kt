package com.jb.leitnerbox.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.csv.MalformedReason
import com.jb.leitnerbox.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportScreen(
    viewModel: CsvImportViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onImportSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { it.readText() }
            content?.let { viewModel.onFileSelected(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.csv_import_title)) },
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
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { openFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.csv_import_select_file))
                }
            }
        }
    }

    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onImportCancelled,
            title = { Text(stringResource(R.string.csv_import_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.csv_import_confirm))
                    if (uiState.mergingDeckNames.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.csv_import_merging_message,
                                uiState.mergingDeckNames.joinToString(", ")
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onImportConfirmed) {
                    Text(stringResource(R.string.csv_import_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onImportCancelled) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    uiState.importResult?.let { result ->
        AlertDialog(
            onDismissRequest = {
                viewModel.onResultDismissed()
                onImportSuccess()
            },
            title = { Text("Succès") },
            text = {
                Column {
                    Text(stringResource(R.string.csv_import_success, result.importedCount))
                    if (result.ignoredLines.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.csv_import_duplicates,
                                result.ignoredLines.size,
                                result.ignoredLines.joinToString(", ")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onResultDismissed()
                    onImportSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.parseError?.let { reason ->
        val message = when (reason) {
            MalformedReason.MISSING_REQUIRED_COLUMN -> stringResource(R.string.csv_import_error_headers)
            MalformedReason.EMPTY_FILE -> stringResource(R.string.csv_import_error_empty)
            else -> stringResource(R.string.csv_import_error_format)
        }
        AlertDialog(
            onDismissRequest = viewModel::onResultDismissed,
            title = { Text(stringResource(R.string.backup_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::onResultDismissed) {
                    Text("OK")
                }
            }
        )
    }
}
