package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    viewModel: DeckDetailViewModel,
    onBackClick: () -> Unit,
    onAddCardClick: (Long) -> Unit,
    onBoxClick: (Long, Int) -> Unit,
    onDeckDeleted: (Deck) -> Unit
) {
    val deck by viewModel.deck.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Détails") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteDeck { deletedDeck ->
                            onDeckDeleted(deletedDeck) // Navigate back immediately
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer le deck")
                    }
                }
            )
        },
        floatingActionButton = {
            deck?.let {
                FloatingActionButton(onClick = { onAddCardClick(it.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une carte")
                }
            }
        }
    ) { padding ->
        deck?.let { currentDeck ->
            if (cards.isEmpty()) {
                EmptyState(
                    message = "Ce deck n'a pas encore de cartes.",
                    actionText = "Ajouter une carte",
                    onActionClick = { onAddCardClick(currentDeck.id) },
                    modifier = Modifier.padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Total : ${cards.size} cartes",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // Removed individual cards from deck screen as they should be in box screen

                    itemsIndexed(currentDeck.intervals) { index, interval ->
                        val boxNumber = index + 1
                        val cardsInBox = cards.count { it.box == boxNumber }
                        val firstCardNextReview = cards.filter { it.box == boxNumber }
                            .mapNotNull { it.nextReviewDate }
                            .minOrNull()
                        
                        ListItem(
                            headlineContent = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Boîte $boxNumber")
                                    if (cardsInBox > 0) {
                                        Text(
                                            text = " - $cardsInBox cartes",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            },
                            supportingContent = { 
                                Column {
                                    if (cardsInBox > 0) {
                                        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                            .withZone(java.time.ZoneId.systemDefault())
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            if (firstCardNextReview != null) {
                                                Text(
                                                    text = "Prochaine session : ${formatter.format(firstCardNextReview)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            } else {
                                                // Should not happen if cardsInBox > 0, but for safety:
                                                Text(
                                                    text = "Prochaine session : N/A",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            
                                            Text(
                                                text = "Tous les $interval j",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Tous les $interval j",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onBoxClick(currentDeck.id, index) }
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
