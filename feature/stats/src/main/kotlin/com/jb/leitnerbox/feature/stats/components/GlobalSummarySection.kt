package com.jb.leitnerbox.feature.stats.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.usecase.stats.GlobalSummary
import com.jb.leitnerbox.feature.stats.R

@Composable
internal fun GlobalSummarySection(summary: GlobalSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatChip(
                label = stringResource(R.string.stats_total_cards),
                value = summary.totalCards.toString()
            )
            StatChip(
                label = stringResource(R.string.stats_mastered),
                value = "${summary.masteredCards} (${
                    if (summary.totalCards > 0)
                        (summary.masteredCards * 100) / summary.totalCards
                    else 0
                } %)"
            )
            StatChip(
                label = stringResource(R.string.stats_success_rate),
                value = summary.globalSuccessRate?.let { "$it %" }
                    ?: stringResource(R.string.stats_no_data)
            )
        }
        Text(
            text = stringResource(R.string.stats_total_sessions, summary.totalSessions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
