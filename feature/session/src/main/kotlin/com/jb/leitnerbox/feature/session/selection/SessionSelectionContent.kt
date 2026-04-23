package com.jb.leitnerbox.feature.session.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.components.EmptyState
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSelectionContent(
    uiState: SessionSelectionUiState,
    onBoxToggled: (SelectableBoxItem) -> Unit,
    onStartSession: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_selection_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.total_cards_to_review,
                                uiState.totalSelectedCards
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onStartSession,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.canStart
                        ) {
                            Text(stringResource(R.string.start_session))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.nothing_to_review_today),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val groupedByDeck = uiState.items
                    .groupBy { it.planItem.deck }
                    .entries
                    .sortedBy { it.key.name }
                
                groupedByDeck.forEach { (deck, boxes) ->
                    item {
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(boxes) { item ->
                        SelectableBoxRow(
                            item = item,
                            onToggle = { onBoxToggled(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableBoxRow(
    item: SelectableBoxItem,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggle() }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.box_number, item.planItem.boxNumber),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.card_count, item.planItem.cardCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
