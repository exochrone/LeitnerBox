package com.jb.leitnerbox.feature.decks.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.DeckProgressBar
import com.jb.leitnerbox.core.ui.components.EmptyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    viewModel: DeckListViewModel,
    onDeckClick: (Long) -> Unit,
    onAddDeckClick: () -> Unit,
    deletedDeck: Deck? = null,
    onUndoDelete: (Deck) -> Unit = {},
    onSnackbarDismissed: () -> Unit = {}
) {
    val decks by viewModel.decks.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Pour éviter que la snackbar ne réapparaisse au retour sur l'écran
    DisposableEffect(Unit) {
        onDispose {
            onSnackbarDismissed()
        }
    }

    LaunchedEffect(deletedDeck) {
        deletedDeck?.let { deck ->
            // Timer pour auto-dismiss après 7 secondes
            val timerJob = launch {
                delay(7000)
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            val result = snackbarHostState.showSnackbar(
                message = "Deck supprimé",
                actionLabel = "Annuler",
                duration = SnackbarDuration.Indefinite
            )

            timerJob.cancel()

            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete(deck)
            }
            
            onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Mes Decks") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDeckClick) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un deck")
            }
        }
    ) { padding ->
        if (decks.isEmpty()) {
            EmptyState(
                message = "Créez votre premier deck pour commencer à apprendre !",
                actionText = "Créer un deck",
                onActionClick = onAddDeckClick,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(decks) { item ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                text = item.deck.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) 
                        },
                        supportingContent = { 
                            Column {
                                Text("${item.deck.intervals.size} boîtes • ${item.cardCount} cartes")
                                Spacer(Modifier.height(4.dp))
                                DeckProgressBar(
                                    progress = item.progress,
                                    showLabel = false
                                )
                            }
                        },
                        modifier = Modifier
                            .clickable { onDeckClick(item.deck.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
