package com.jb.leitnerbox.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.usecase.stats.DeckStat
import com.jb.leitnerbox.feature.stats.R

@Composable
internal fun DeckStatsSection(deckStats: List<DeckStat>) {
    if (deckStats.isEmpty()) {
        Text(stringResource(R.string.stats_no_decks))
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        deckStats.forEach { stat ->
            DeckStatCard(stat = stat)
        }
    }
}

@Composable
private fun DeckStatCard(stat: DeckStat) {
    var expanded by remember { mutableStateOf(false) }
    val masteryRate = if (stat.totalCards > 0)
        stat.masteredCards.toFloat() / stat.totalCards else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stat.deck.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(masteryRate * 100).toInt()} %",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { masteryRate },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.stats_deck_summary,
                    stat.masteredCards, stat.totalCards
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                stat.cardsByBox.entries.sortedBy { it.key }.forEach { (box, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.stats_box_label, box),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.stats_card_count, count),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
