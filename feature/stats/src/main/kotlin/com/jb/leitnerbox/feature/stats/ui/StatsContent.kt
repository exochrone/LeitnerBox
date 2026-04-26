package com.jb.leitnerbox.feature.stats.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.components.EmptyState
import com.jb.leitnerbox.feature.stats.R
import com.jb.leitnerbox.feature.stats.components.DeckStatsSection
import com.jb.leitnerbox.feature.stats.components.GlobalSummarySection
import com.jb.leitnerbox.feature.stats.components.SessionHistorySection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsContent(
    uiState: StatsUiState,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.hasNoData -> {
                EmptyState(
                    message = stringResource(R.string.stats_empty_global),
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    uiState.globalSummary?.let { summary ->
                        GlobalSummarySection(summary = summary)
                        HorizontalDivider()
                    }
                    
                    Text(
                        text = "Par deck",
                        style = MaterialTheme.typography.titleMedium
                    )
                    DeckStatsSection(deckStats = uiState.deckStats)
                    
                    HorizontalDivider()
                    
                    Text(
                        text = "Historique",
                        style = MaterialTheme.typography.titleMedium
                    )
                    SessionHistorySection(items = uiState.sessionHistory)
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
