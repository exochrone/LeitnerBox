package com.jb.leitnerbox.feature.importexport.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.csv.DuplicateEntry
import com.jb.leitnerbox.core.domain.csv.DuplicateSource
import com.jb.leitnerbox.feature.importexport.R

@Composable
fun DuplicatesDialog(
    duplicates: List<DuplicateEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.import_duplicates_title, duplicates.size))
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                duplicates.forEach { entry ->
                    val sourceLabel = when (entry.source) {
                        DuplicateSource.WITHIN_FILE ->
                            stringResource(R.string.import_duplicate_in_file)
                        DuplicateSource.WITH_DECK ->
                            stringResource(R.string.import_duplicate_in_deck)
                    }
                    Text(
                        text = stringResource(
                            R.string.import_duplicate_entry,
                            entry.lineNumber,
                            entry.question,
                            sourceLabel
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
