package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.feature.dashboard.R

@Composable
internal fun DashboardContent(
    uiState: DashboardUiState,
    onStartSession: () -> Unit,
    onStatsClick: () -> Unit,
    onChallengeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (uiState) {
            DashboardUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is DashboardUiState.Empty -> {
                EmptyDashboard(
                    streak = uiState.streak,
                    onStatsClick = onStatsClick,
                    onChallengeClick = onChallengeClick
                )
            }
            is DashboardUiState.Success -> {
                SessionSummary(
                    totalCards = uiState.totalCardsToReview,
                    deckCount = uiState.decksWithReviews,
                    streak = uiState.streak,
                    onStartSession = onStartSession,
                    onStatsClick = onStatsClick,
                    onChallengeClick = onChallengeClick
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboard(
    streak: Int,
    onStatsClick: () -> Unit,
    onChallengeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StreakBadge(streak = streak)
        
        Row {
            TextButton(onClick = onStatsClick) {
                Text("Voir mes statistiques")
            }
            TextButton(onClick = onChallengeClick) {
                Text("Challenge")
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.dashboard_empty_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.dashboard_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StreakBadge(streak: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "🔥",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (streak == 0)
                stringResource(R.string.dashboard_streak_zero)
            else
                stringResource(R.string.dashboard_streak, streak),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SessionSummary(
    totalCards: Int,
    deckCount: Int,
    streak: Int,
    onStartSession: () -> Unit,
    onStatsClick: () -> Unit,
    onChallengeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StreakBadge(streak = streak)

        Row {
            TextButton(onClick = onStatsClick) {
                Text("Voir mes statistiques")
            }
            TextButton(onClick = onChallengeClick) {
                Text("Challenge")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dashboard_cards_to_review, totalCards),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_deck_distribution, deckCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.dashboard_start_session))
        }
    }
}
