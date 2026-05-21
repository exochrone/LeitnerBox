package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.components.MathText
import com.jb.leitnerbox.feature.decks.R
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
                title = {
                    val title = if (viewModel.boxNumber == 0) {
                        stringResource(R.string.deck_detail_buffer_title)
                    } else {
                        stringResource(R.string.box_label, viewModel.boxNumber)
                    }
                    Text(title)
                },
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
                itemsIndexed(cards) { index, card ->
                    val backgroundColor = if (index % 2 == 1) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    } else {
                        Color.Transparent
                    }
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = backgroundColor
                        ),
                        headlineContent = {
                            Column {
                                MathText(
                                    text = card.recto,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(4.dp))
                                MathText(
                                    text = card.verso,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        supportingContent = {
                            Text(
                                text = "Saisie requise : ${if (card.needsInput) "Oui" else "Non"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        modifier = Modifier
                            .background(backgroundColor)
                            .clickable { onEditCardClick(card.id) }
                    )
                }
            }
        }
    }
}