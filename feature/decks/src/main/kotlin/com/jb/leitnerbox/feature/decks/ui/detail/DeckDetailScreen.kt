package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.ui.components.ColorPickerDialog
import com.jb.leitnerbox.core.ui.components.DeckProgressBar
import com.jb.leitnerbox.core.ui.components.EmptyState
import com.jb.leitnerbox.core.ui.theme.DEFAULT_DECK_COLOR
import com.jb.leitnerbox.core.ui.theme.DefaultDeckColorDark
import com.jb.leitnerbox.core.ui.utils.LeitnerColorUtils
import com.jb.leitnerbox.core.ui.utils.resolveColor
import com.jb.leitnerbox.feature.decks.R
import com.jb.leitnerbox.feature.decks.utils.DeckDateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    viewModel: DeckDetailViewModel,
    onBackClick: () -> Unit,
    onAddCardClick: (Long) -> Unit,
    onEditDeckClick: (Long) -> Unit,
    onBoxClick: (Long, Int) -> Unit,
    onDeckDeleted: (Deck, List<Card>) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showColorPicker by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showActivateDialog by remember { mutableStateOf(false) }
    
    val deckColor = uiState.deck?.resolveColor() ?: MaterialTheme.colorScheme.onSurface
    val boxCount = uiState.deck?.intervals?.size ?: 5
    val summaryTextColor = MaterialTheme.colorScheme.surface
    
    val boxDarkColor = if (uiState.deck?.color == DEFAULT_DECK_COLOR)
        DefaultDeckColorDark
    else
        deckColor
        
    val couleur2 = LeitnerColorUtils.boxColor(
        boxIndex = 1,
        totalBoxes = boxCount,
        darkColor = boxDarkColor
    )

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

    if (showRenameDialog) {
        RenameDeckDialog(
            currentName = uiState.deck?.name ?: "",
            onConfirm = { newName ->
                viewModel.renameDeck(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showActivateDialog) {
        ActivateCardsDialog(
            inactiveCount = uiState.inactiveCardCount,
            defaultCount = uiState.newCardsPerDay,
            onConfirm = { count ->
                viewModel.activateCards(count)
                showActivateDialog = false
            },
            onDismiss = { showActivateDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.deck?.name ?: "Détails",
                        color = deckColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showRenameDialog = true }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = deckColor
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { uiState.deck?.id?.let { onEditDeckClick(it) } },
                        modifier = Modifier.width(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.deck_edit_title_edit),
                            tint = deckColor
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteDeck { deck, cards ->
                                onDeckDeleted(deck, cards)
                            }
                        },
                        modifier = Modifier.width(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer le deck",
                            tint = deckColor
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            uiState.deck?.let {
                FloatingActionButton(
                    onClick = { onAddCardClick(it.id) },
                    containerColor = deckColor,
                    contentColor = summaryTextColor
                ) {
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
                CircularProgressIndicator(color = deckColor)
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
                            .fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = deckColor
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${uiState.cards.size} cartes",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = summaryTextColor
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    DeckProgressBar(
                                        progress = uiState.progress,
                                        color = couleur2,
                                        labelColor = summaryTextColor
                                    )
                                }
                            }
                        }

                        if (uiState.inactiveCardCount > 0) {
                            item {
                                CartesEnReserveCard(
                                    count = uiState.inactiveCardCount,
                                    deckColor = deckColor,
                                    onClick = { showActivateDialog = true }
                                )
                            }
                        }

                        itemsIndexed(currentDeck.intervals) { index, interval ->
                            val boxNumber = index + 1
                            val cardsInBox = uiState.cards.count { it.box == boxNumber }
                            val firstCardNextReview = uiState.cards.filter { it.box == boxNumber }
                                .mapNotNull { it.nextReviewDate }
                                .minOrNull()
                            
                            val locale = LocalConfiguration.current.locales[0]
                            val dateLabel = remember(firstCardNextReview) {
                                DeckDateFormatter.format(firstCardNextReview, locale)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable { onBoxClick(currentDeck.id, index) },
                                colors = CardDefaults.cardColors(
                                    containerColor = couleur2
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Boîte $boxNumber",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = deckColor
                                        )
                                        if (cardsInBox > 0) {
                                            Text(
                                                text = "$cardsInBox cartes",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = deckColor
                                            )
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(4.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (cardsInBox > 0) "Prochaine session : $dateLabel" else "Vide",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "Tous les $interval j",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Black,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenameDeckDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renommer le deck") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nouveau nom") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank()
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun CartesEnReserveCard(
    count: Int,
    deckColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = deckColor.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, deckColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cartes en réserve",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = deckColor
            )
            Text(
                text = "$count cartes inactives",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActivateCardsDialog(
    inactiveCount: Int,
    defaultCount: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember {
        mutableStateOf(minOf(defaultCount, inactiveCount).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activer des cartes") },
        text = {
            Column {
                Text("Combien de cartes voulez-vous activer maintenant ?")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.all { c -> c.isDigit() }) text = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    label = { Text("Nombre de cartes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Maximum : $inactiveCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            val count = text.toIntOrNull() ?: 0
            TextButton(
                onClick = { onConfirm(count.coerceIn(1, inactiveCount)) },
                enabled = count > 0
            ) {
                Text("Activer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
