package com.jb.leitnerbox.feature.cards.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.utils.LatexDetector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    viewModel: CardEditViewModel,
    onBackClick: () -> Unit,
    onCardSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.events.collect { event ->
            when (event) {
                CardEditEvent.CardSaved -> onCardSaved()
                CardEditEvent.CardSavedAndNext -> {
                    focusRequester.requestFocus()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle Carte") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.recto,
                onValueChange = viewModel::onRectoChange,
                label = { Text("Recto (Question)") },
                isError = uiState.rectoError || uiState.errorMessage != null,
                supportingText = {
                    if (uiState.rectoError) {
                        Text("Le recto est obligatoire")
                    } else if (uiState.errorMessage != null) {
                        Text(uiState.errorMessage!!)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.verso,
                onValueChange = viewModel::onVersoChange,
                label = { Text("Verso (Réponse)") },
                isError = uiState.versoError,
                supportingText = {
                    if (uiState.versoError) {
                        Text("Le verso est obligatoire")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.saveCard(andNext = true) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Enregistrer + carte", maxLines = 1)
                }

                Button(
                    onClick = { viewModel.saveCard(andNext = false) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enregistrer")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.needsInput,
                    onCheckedChange = viewModel::onNeedsInputChange,
                    enabled = !LatexDetector.containsLatex(uiState.verso)
                )
                Text(
                    text = "Saisie requise",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (LatexDetector.containsLatex(uiState.verso)) {
                Text(
                    text = "Le verso contient une formule LaTeX — la saisie clavier est désactivée automatiquement.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}
