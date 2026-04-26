package com.jb.leitnerbox.feature.importexport.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.time.LocalDate

@Composable
fun ImportExportScreen(
    viewModel: ImportExportViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher SAF
    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val content = context.contentResolver
                .openInputStream(it)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { reader -> reader.readText() }
                ?: return@let
            viewModel.onFileContent(content)
        }
    }

    // Events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ImportExportEvent.ShareCsv -> {
                    val fileName = "${event.deckName.replace(" ", "_")}_" +
                        "${LocalDate.now()}.csv"
                    val file = File(context.cacheDir, fileName)
                    file.writeText(event.csvContent)
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                }
            }
        }
    }

    ImportExportContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onExportClick = viewModel::onExportClick,
        onImportClick = { openFileLauncher.launch(arrayOf("text/*")) },
        onResultDismissed = viewModel::onResultDismissed
    )
}
