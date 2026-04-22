package com.jb.leitnerbox.core.ui.components.session

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SessionProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label ?: "Carte $current / $total",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) current.toFloat() / total.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
