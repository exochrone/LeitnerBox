package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.ui.components.FlipCard
import com.jb.leitnerbox.core.ui.components.SessionProgressIndicator
import com.jb.leitnerbox.core.ui.components.SwipeableCard
import com.jb.leitnerbox.core.ui.theme.SuccessGreen
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionContent(
    uiState: SessionUiState,
    onFlip: () -> Unit,
    onEvaluate: (Boolean) -> Unit,
    onInputChanged: (String) -> Unit,
    onInputValidated: () -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val progressLabel = if (uiState.isChallenge) {
                stringResource(R.string.session_mastered_label, uiState.progressCurrent, uiState.progressTotal)
            } else {
                stringResource(R.string.session_progress_label, uiState.currentIndex + 1, uiState.progressTotal)
            }
            SessionProgressIndicator(
                current = uiState.progressCurrent,
                total = uiState.progressTotal,
                label = progressLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (uiState.isMasteredTransition) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    MasteryMessage(uiState.currentDeckName)
                }
            } else {
                uiState.currentCard?.let { card ->
                    key(card.id) {
                        SwipeableCard(
                            isFlipped = uiState.isFlipped && !card.needsInput,
                            onEvaluate = onEvaluate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 250.dp, max = 400.dp)
                                .padding(vertical = 16.dp)
                        ) {
                            FlipCard(
                                recto = card.recto,
                                verso = card.verso,
                                isFlipped = uiState.isFlipped,
                                onFlip = {
                                    if (!card.needsInput) {
                                        onFlip()
                                    } else if (uiState.inputValidated) {
                                        onContinue()
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                rectoLabel = uiState.currentDeckName,
                                versoLabel = stringResource(R.string.session_answer_label)
                            )
                        }
                    }

                    if (card.needsInput) {
                        InputSection(
                            uiState = uiState,
                            onInputChanged = onInputChanged,
                            onInputValidated = onInputValidated,
                            onContinue = onContinue
                        )
                    } else {
                        if (uiState.isFlipped) {
                            EvaluationButtons(onEvaluate = onEvaluate)
                        } else {
                            Button(
                                onClick = onFlip,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.session_flip_card))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasteryMessage(deckName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = deckName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.session_card_mastered),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.session_mastered_congrats),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InputSection(
    uiState: SessionUiState,
    onInputChanged: (String) -> Unit,
    onInputValidated: () -> Unit,
    onContinue: () -> Unit
) {
    if (!uiState.inputValidated) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.userInput,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onInputValidated() }),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.session_input_placeholder)) }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onInputValidated) {
                Text(stringResource(R.string.session_ok))
            }
        }
    } else {
        val isCorrect = uiState.checkResult is AnswerCheckResult.Correct
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isCorrect) stringResource(R.string.session_correct) else stringResource(R.string.session_incorrect),
                color = if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
            if (!isCorrect) {
                Text(
                    text = stringResource(R.string.session_your_input, uiState.userInput),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.session_continue))
            }
        }
    }
}

@Composable
private fun EvaluationButtons(
    onEvaluate: (Boolean) -> Unit
) {
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
