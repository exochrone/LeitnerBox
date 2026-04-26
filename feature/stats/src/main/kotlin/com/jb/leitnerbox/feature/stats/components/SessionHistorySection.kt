package com.jb.leitnerbox.feature.stats.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.usecase.stats.SessionHistoryItem
import com.jb.leitnerbox.feature.stats.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun SessionHistorySection(items: List<SessionHistoryItem>) {
    if (items.isEmpty()) {
        Text(stringResource(R.string.stats_empty_history))
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            SessionHistoryCard(item = item)
        }
    }
}

@Composable
private fun SessionHistoryCard(item: SessionHistoryItem) {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val date = item.session.date
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)

    val deckNamesText = when {
        item.deckNames.isEmpty() -> stringResource(R.string.stats_unknown_deck)
        item.deckNames.size == 1 -> item.deckNames.first()
        else -> stringResource(
            R.string.stats_multiple_decks,
            item.deckNames.first(),
            item.deckNames.size - 1
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${item.successRate} %",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.successRate >= 60) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = deckNamesText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.stats_cards_reviewed, item.session.cardCount),
                    style = MaterialTheme.typography.bodySmall
                )
                if (item.session.masteredCount > 0) {
                    Text(
                        text = "🎓 ${item.session.masteredCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
