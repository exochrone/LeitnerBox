package com.jb.leitnerbox.feature.decks.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.EmptyState
import com.jb.leitnerbox.feature.decks.R
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

    DisposableEffect(Unit) {
        onDispose {
            onSnackbarDismissed()
        }
    }

    LaunchedEffect(deletedDeck) {
        deletedDeck?.let { deck ->
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
            TopAppBar(title = { Text(stringResource(R.string.deck_list_title, decks.size)) })
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
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 80.dp // Extra padding for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = decks,
                    key = { it.deck.id }
                ) { item ->
                    DeckListItem(
                        item = item,
                        onClick = { onDeckClick(item.deck.id) }
                    )
                }
            }
        }
    }
}
