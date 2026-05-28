package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.utils.LatexDetector
import com.jb.leitnerbox.core.ui.components.SessionProgressIndicator
import com.jb.leitnerbox.core.ui.components.SwipeableFlipCard2
import com.jb.leitnerbox.core.ui.theme.SuccessGreen
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Session2Content(
    uiState: SessionUiState,
    onFlip: () -> Unit,
    onEvaluate: (Boolean) -> Unit,
    onToggleTts: () -> Unit,
    onToggleButtons: () -> Unit,
    onSpeak: (String) -> Unit,
    onZoomChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val currentText = if (uiState.isFlipped) {
        uiState.currentCard?.verso ?: ""
    } else {
        uiState.currentCard?.recto ?: ""
    }
    val hasLatex = LatexDetector.containsLatex(currentText)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session 2") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (hasLatex) {
                        IconButton(onClick = { onSpeak("Impossible de lire les expressions mathématiques") }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "LaTeX - Pas de son"
                            )
                        }
                    } else {
                        IconButton(onClick = onToggleTts) {
                            Icon(
                                imageVector = if (uiState.isTextToSpeechEnabled) Icons.Default.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                tint = if (uiState.isTextToSpeechEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                contentDescription = if (uiState.isTextToSpeechEnabled) {
                                    stringResource(R.string.session_tts_disable)
                                } else {
                                    stringResource(R.string.session_tts_enable)
                                }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (uiState.showButtons)
                                            stringResource(R.string.session2_menu_hide_buttons)
                                        else
                                            stringResource(R.string.session2_menu_show_buttons)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleButtons()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (uiState.showButtons)
                                            Icons.Default.VisibilityOff
                                        else
                                            Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.session2_menu_zoom_in)) },
                                onClick = {
                                    // showMenu reste true : le menu reste ouvert
                                    onZoomChange(true)
                                },
                                leadingIcon = { Icon(Icons.Default.ZoomIn, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.session2_menu_zoom_out)) },
                                onClick = {
                                    // showMenu reste true : le menu reste ouvert
                                    onZoomChange(false)
                                },
                                leadingIcon = { Icon(Icons.Default.ZoomOut, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Barre de progression avec compteur au-dessus
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Carte ${uiState.currentIndex + 1} / ${uiState.progressTotal}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                SessionProgressIndicator(
                    current = uiState.progressCurrent,
                    total = uiState.progressTotal,
                    label = "", // Label vide car on a le texte au dessus
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Titre du deck et Sous-titre
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.currentDeckName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                uiState.currentCard?.let { card ->
                    Text(
                        text = "Boîte ${card.box}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Carte carrée
            uiState.currentCard?.let { card ->
                key(card.id) {
                    SwipeableFlipCard2(
                        recto = card.recto,
                        verso = card.verso,
                        isFlipped = uiState.isFlipped,
                        onFlip = onFlip,
                        onEvaluate = onEvaluate,
                        rectoZoom = card.rectoZoom,
                        versoZoom = card.versoZoom,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Boutons d'action
            if (uiState.showButtons) {
                if (!uiState.isFlipped) {
                    Button(
                        onClick = onFlip,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.session_flip_card))
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onEvaluate(false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = stringResource(R.string.eval_wrong)
                            )
                        }

                        Button(
                            onClick = { onEvaluate(true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = stringResource(R.string.eval_correct)
                            )
                        }
                    }
                }
            }
        }
    }
}
