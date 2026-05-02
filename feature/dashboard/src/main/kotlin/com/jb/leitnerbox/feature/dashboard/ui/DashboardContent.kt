package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.DashboardGlobalStats
import com.jb.leitnerbox.feature.dashboard.R

@Composable
internal fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToSessionSelection: () -> Unit,
    onNavigateToDecks: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bloc statistiques
        uiState.stats?.let { StatsBlock(stats = it) }

        // Carte sessions du jour — masquée si aucune session
        if (uiState.sessionPlan.items.isNotEmpty()) {
            val cardCount  = uiState.sessionPlan.items.sumOf { it.cardCount }
            val deckCount  = uiState.sessionPlan.items.map { it.deck.id }.distinct().size
            ActionCard(
                icon     = ImageVector.vectorResource(R.drawable.ic_cards_stack),
                title    = stringResource(R.string.dashboard_cards_to_review_count, cardCount),
                subtitle = stringResource(R.string.dashboard_decks_distribution, deckCount),
                onClick  = onNavigateToSessionSelection
            )
        }

        // Carte decks — toujours visible
        val deckCount = uiState.stats?.deckCount ?: 0
        ActionCard(
            icon    = ImageVector.vectorResource(R.drawable.ic_stacks),
            title   = if (deckCount == 0)
                stringResource(R.string.dashboard_no_decks)
            else
                stringResource(R.string.dashboard_deck_count, deckCount),
            onClick = onNavigateToDecks
        )
    }
}

@Composable
private fun StatsBlock(stats: DashboardGlobalStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ligne 1 : 🔥 | Cartes | Maîtrisées (Libellés en haut, Valeurs en bas)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LabelItem(
                        text = "🔥",
                        color = if (stats.streak == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current,
                        style = MaterialTheme.typography.titleMedium
                    )
                    LabelItem(text = stringResource(R.string.stats_total_cards))
                    LabelItem(text = stringResource(R.string.stats_mastered))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ValueItem(
                        text = stats.streak.toString(),
                        color = if (stats.streak == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current
                    )
                    ValueItem(text = stats.totalCards.toString())
                    ValueItem(
                        text = if (stats.masteredCards > 0)
                            "${stats.masteredCards} (${stats.masteredPercent}%)"
                        else
                            stats.masteredCards.toString()
                    )
                }
            }

            HorizontalDivider()

            // Ligne 2 : Bonnes réponses | Progression | Sessions
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.stats_success_rate),
                    value = stats.successRate?.let { "$it%" }
                        ?: stringResource(R.string.stats_no_data)
                )
                StatItem(
                    label = stringResource(R.string.stats_progression),
                    value = "${stats.progressionGlobal}%"
                )
                StatItem(
                    label = stringResource(R.string.stats_sessions),
                    value = stats.sessionCount.toString()
                )
            }
        }
    }
}

@Composable
private fun LabelItem(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall
) {
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.width(80.dp) // Largeur fixe pour alignement vertical entre les deux lignes
    )
}

@Composable
private fun ValueItem(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier.width(80.dp)
    )
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color = LocalContentColor.current,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall,
    reverseOrder: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (reverseOrder) {
            if (label.isNotEmpty()) {
                Text(
                    text  = label,
                    style = labelStyle,
                    color = if (labelStyle == MaterialTheme.typography.labelSmall) 
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else color
                )
            }
            Text(
                text  = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (label.isNotEmpty()) {
                Text(
                    text  = label,
                    style = labelStyle,
                    color = if (labelStyle == MaterialTheme.typography.labelSmall) 
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else color
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier              = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(40.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle != null) {
                        Text(
                            text  = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Icon(
                imageVector        = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier           = Modifier.size(32.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
        }
    }
}
