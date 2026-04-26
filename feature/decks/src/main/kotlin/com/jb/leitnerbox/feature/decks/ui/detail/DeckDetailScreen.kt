package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.ColorPickerDialog
import com.jb.leitnerbox.core.ui.components.DeckProgressBar
import com.jb.leitnerbox.core.ui.components.EmptyState
import com.jb.leitnerbox.core.ui.theme.DEFAULT_DECK_COLOR
import com.jb.leitnerbox.core.ui.utils.resolveColor
import com.jb.leitnerbox.feature.decks.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    viewModel: DeckDetailViewModel,
    onBackClick: () -> Unit,
    onAddCardClick: (Long) -> Unit,
    onBoxClick: (Long, Int) -> Unit,
    onDeckDeleted: (Deck) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColorHex = uiState.deck?.color ?: DEFAULT_DECK_COLOR,
            onColorSelected = { hex ->
                viewModel.onColorSelected(hex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.deck?.name ?: "Détails") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = stringResource(R.string.deck_color_picker_cd),
                            tint = uiState.deck?.resolveColor() ?: MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        viewModel.deleteDeck { deletedDeck ->
                            onDeckDeleted(deletedDeck)
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer le deck")
                    }
                }
            )
        },
        floatingActionButton = {
            uiState.deck?.let {
                FloatingActionButton(onClick = { onAddCardClick(it.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une carte")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.deck?.let { currentDeck ->
                if (uiState.cards.isEmpty()) {
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
                                        "Total : ${uiState.cards.size} cartes",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    DeckProgressBar(
                                        progress = uiState.progress,
                                        // On pourrait aussi colorer cette barre via DeckProgressBar
                                    )
                                }
                            }
                        }

                        itemsIndexed(currentDeck.intervals) { index, interval ->
                            val boxNumber = index + 1
                            val cardsInBox = uiState.cards.count { it.box == boxNumber }
                            val firstCardNextReview = uiState.cards.filter { it.box == boxNumber }
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
                                                color = currentDeck.resolveColor(),
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
            }
        }
    }
}
