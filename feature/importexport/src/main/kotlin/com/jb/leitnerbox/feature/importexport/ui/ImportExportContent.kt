package com.jb.leitnerbox.feature.importexport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.feature.importexport.R
import com.jb.leitnerbox.feature.importexport.components.DuplicatesDialog
import com.jb.leitnerbox.feature.importexport.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImportExportContent(
    uiState: ImportExportUiState,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResultDismissed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section Export
            ExportSection(
                cardCount = uiState.exportCardCount,
                onExportClick = onExportClick,
                isLoading = uiState.isLoading
            )

            HorizontalDivider()

            // Section Import
            ImportSection(
                deckName = uiState.targetDeckName,
                onImportClick = onImportClick,
                isLoading = uiState.isLoading
            )
        }

        // Handle Results
        when (val result = uiState.result) {
            is ImportUiResult.Success -> {
                AlertDialog(
                    onDismissRequest = onResultDismissed,
                    title = { Text(stringResource(R.string.import_success_title)) },
                    text = { Text(stringResource(R.string.import_success, result.count)) },
                    confirmButton = {
                        TextButton(onClick = onResultDismissed) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }
            is ImportUiResult.DuplicatesFound -> {
                DuplicatesDialog(
                    duplicates = result.entries,
                    onDismiss = onResultDismissed
                )
            }
            ImportUiResult.MalformedFile,
            ImportUiResult.EncodingError,
            ImportUiResult.EmptyFile,
            ImportUiResult.MissingColumns -> {
                val message = when (result) {
                    ImportUiResult.EmptyFile -> stringResource(R.string.import_error_empty)
                    ImportUiResult.EncodingError -> stringResource(R.string.import_error_encoding)
                    ImportUiResult.MissingColumns -> stringResource(R.string.import_error_missing_columns)
                    else -> stringResource(R.string.import_error_format)
                }
                ErrorDialog(message = message, onDismiss = onResultDismissed)
            }
            null -> Unit
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ExportSection(
    cardCount: Int,
    onExportClick: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.export_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (cardCount > 0) 
                stringResource(R.string.export_card_count, cardCount)
            else 
                stringResource(R.string.export_empty_deck),
            style = MaterialTheme.typography.bodyMedium
        )
        Button(
            onClick = onExportClick,
            enabled = cardCount > 0 && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.export_button))
        }
    }
}

@Composable
private fun ImportSection(
    deckName: String,
    onImportClick: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.import_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.import_deck_target, deckName),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(
            onClick = onImportClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.import_select_file))
        }
    }
}
