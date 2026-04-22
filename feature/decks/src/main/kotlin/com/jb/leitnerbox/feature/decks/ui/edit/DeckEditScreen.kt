package com.jb.leitnerbox.feature.decks.ui.edit

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckEditScreen(
    viewModel: DeckEditViewModel,
    onBackClick: () -> Unit,
    onDeckSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.events.collect { event ->
            when (event) {
                DeckEditEvent.DeckSaved -> onDeckSaved()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau Deck") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nom du deck") },
                isError = uiState.nameError,
                supportingText = {
                    if (uiState.nameError) {
                        Text("Le nom est obligatoire")
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

            Button(
                onClick = viewModel::saveDeck,
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "En cas de mauvaise réponse",
                style = MaterialTheme.typography.titleMedium
            )

            Column(Modifier.selectableGroup()) {
                WrongAnswerRuleRow(
                    text = "Retour en boite 1",
                    selected = uiState.wrongAnswerRule == WrongAnswerRule.BACK_TO_BOX_ONE,
                    onClick = { viewModel.onWrongAnswerRuleChange(WrongAnswerRule.BACK_TO_BOX_ONE) }
                )
                WrongAnswerRuleRow(
                    text = "Retour en boite précédente",
                    selected = uiState.wrongAnswerRule == WrongAnswerRule.PREVIOUS_BOX,
                    onClick = { viewModel.onWrongAnswerRuleChange(WrongAnswerRule.PREVIOUS_BOX) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Configuration des boîtes (par défaut)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                "5 boîtes : 1, 3, 5, 7, 14 jours",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun WrongAnswerRuleRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because the row is selectable
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
