package com.jb.leitnerbox.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.jb.leitnerbox.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvExportScreen(
    viewModel: CsvExportViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val decks by viewModel.decks.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedDeckIds.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.onFileCreated(it, context.contentResolver) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CsvExportEvent.RequestCreateFile -> {
                    createDocumentLauncher.launch(event.fileName)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.csv_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = viewModel::onExportClick,
                    enabled = selectedIds.isNotEmpty() && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.csv_export_validate))
                    }
                }
            }
        }
    ) { padding ->
        if (decks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.stats_no_decks))
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.csv_export_select_decks),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(decks) { deck ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onDeckToggled(deck.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = deck.id in selectedIds,
                            onCheckedChange = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(deck.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (uiState.showSuccess && uiState.lastExportInfo != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissSuccess,
            title = { Text(stringResource(R.string.csv_import_success_title)) },
            text = { 
                Text(stringResource(
                    R.string.csv_export_success, 
                    uiState.lastExportInfo!!.first, 
                    uiState.lastExportInfo!!.second
                )) 
            },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissSuccess) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissError,
            title = { Text(stringResource(R.string.backup_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissError) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}
