package com.jb.leitnerbox.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.R

@Composable
fun SessionModeDialog(
    onDismiss: () -> Unit,
    onModeSelected: (Int) -> Unit // 1 pour classique, 2 pour V2
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.session_mode_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onModeSelected(1) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.session_mode_classic))
                }
                Button(
                    onClick = { onModeSelected(2) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.session_mode_v2))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.back))
            }
        }
    )
}
