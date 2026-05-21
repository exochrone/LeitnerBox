package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.DashboardGlobalStats
import com.jb.leitnerbox.feature.dashboard.R
import com.jb.leitnerbox.feature.dashboard.ui.components.MenuActionCard

@Composable
internal fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToSessionSelection: () -> Unit,
    onNavigateToDecks: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onChallengeCardTrigger: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.showChallengeWarningDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(text = stringResource(R.string.challenge_title)) },
            text = { Text(text = stringResource(R.string.challenge_requirement_error)) },
            confirmButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                subtitle = pluralStringResource(R.plurals.dashboard_decks_distribution, deckCount, deckCount),
                onClick  = onNavigateToSessionSelection
            )
        }

        // 1. Carte principale "X deck(s)"
        MenuActionCard(
            title = pluralStringResource(R.plurals.deck_count, uiState.totalDecksCount, uiState.totalDecksCount),
            icon = ImageVector.vectorResource(R.drawable.ic_stacks),
            onClick = onNavigateToDecks
        )

        // 2. Carte d'action "Historique"
        MenuActionCard(
            title = stringResource(R.string.menu_history),
            subtitle = stringResource(R.string.history_subtitle),
            icon = Icons.Default.History,
            onClick = onNavigateToHistory
        )

        // 3. Carte d'action "Challenge"
        val isChallengeAvailable = uiState.masteredCardCount >= 2
        MenuActionCard(
            title = stringResource(R.string.menu_challenge),
            subtitle = stringResource(R.string.challenge_subtitle),
            icon = Icons.Default.WorkspacePremium,
            isIconMuted = !isChallengeAvailable,
            onClick = {
                if (isChallengeAvailable) onNavigateToChallenge() else onChallengeCardTrigger()
            }
        )

        // 4. Carte d'action "Paramètres"
        MenuActionCard(
            title = stringResource(R.string.menu_settings),
            icon = Icons.Default.Settings,
            onClick = onNavigateToSettings
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    LabelItem(
                        text = "🔥",
                        color = if (stats.streak == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    LabelItem(
                        text = stringResource(R.string.stats_total_cards),
                        modifier = Modifier.weight(1f)
                    )
                    LabelItem(
                        text = stringResource(R.string.stats_mastered),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    ValueItem(
                        text = stats.streak.toString(),
                        color = if (stats.streak == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else LocalContentColor.current,
                        modifier = Modifier.weight(1f)
                    )
                    ValueItem(
                        text = stats.totalCards.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ValueItem(
                        text = if (stats.masteredCards > 0)
                            "${stats.masteredCards} (${stats.masteredPercent}%)"
                        else
                            stats.masteredCards.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // Ligne 2 : Bonnes réponses | Progression | Sessions
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = stringResource(R.string.stats_success_rate),
                    value = stats.successRate?.let { "$it%" }
                        ?: stringResource(R.string.stats_no_data),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.stats_progression),
                    value = "${stats.progressionGlobal}%",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.stats_sessions),
                    value = stats.sessionCount.toString(),
                    modifier = Modifier.weight(1f)
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
    Box(
        modifier = modifier.height(32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ValueItem(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    Box(
        modifier = modifier.height(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    labelStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelSmall
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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
