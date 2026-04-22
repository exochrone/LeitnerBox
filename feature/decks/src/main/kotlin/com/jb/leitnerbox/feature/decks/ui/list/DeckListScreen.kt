package com.jb.leitnerbox.feature.decks.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    viewModel: DeckListViewModel,
    onDeckClick: (Long) -> Unit,
    onAddDeckClick: () -> Unit,
    deletedDeck: Deck? = null,
    onUndoDelete: (Deck) -> Unit = {}
) {
    val decks by viewModel.decks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deletedDeck) {
        deletedDeck?.let { deck ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Deck supprimé",
                    actionLabel = "Annuler",
                    duration = SnackbarDuration.Long // 7s approximate or Long is closest to what user wants if they want it long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onUndoDelete(deck)
                }
            }
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
                        headlineContent = { Text(item.deck.name) },
                        supportingContent = { 
                            Text("${item.deck.intervals.size} boîtes • ${item.cardCount} cartes") 
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
