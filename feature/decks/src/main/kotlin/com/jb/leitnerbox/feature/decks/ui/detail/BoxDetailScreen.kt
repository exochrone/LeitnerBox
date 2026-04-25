package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    viewModel: BoxDetailViewModel,
    onBackClick: () -> Unit,
    onEditCardClick: (Long) -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Boîte ${viewModel.boxNumber}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Aucune carte dans cette boîte")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(cards) { card ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = card.recto,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Saisie requise : ${if (card.needsInput) "Oui" else "Non"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                viewModel.deleteCard(card)
                                scope.launch {
                                    val timerJob = launch {
                                        delay(7000)
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Carte supprimée",
                                        actionLabel = "Annuler",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                    timerJob.cancel()
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete(card)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                            }
                        },
                        modifier = Modifier.clickable { onEditCardClick(card.id) }
                    )
                }
            }
        }
    }
}