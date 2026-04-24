package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.components.FlipCard
import com.jb.leitnerbox.core.ui.components.SwipeableCard
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionContent(
    uiState: SessionUiState,
    onFlip: () -> Unit,
    onEvaluate: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            uiState.currentCard?.let { card ->
                key(card.id) {
                    SwipeableCard(
                        isFlipped = uiState.isFlipped && !card.needsInput,
                        onEvaluate = onEvaluate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp)
                    ) {
                        FlipCard(
                            recto = card.recto,
                            verso = card.verso,
                            isFlipped = uiState.isFlipped,
                            onFlip = onFlip,
                            modifier = Modifier.fillMaxSize(),
                            rectoLabel = stringResource(
                                R.string.session_card_label,
                                uiState.currentIndex + 1,
                                uiState.totalCards
                            ),
                            versoLabel = stringResource(R.string.session_answer_label)
                        )
                    }
                }

                if (uiState.isFlipped && !card.needsInput) {
                    EvaluationButtons(onEvaluate = onEvaluate)
                } else if (!uiState.isFlipped) {
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
            Text(stringResource(R.string.eval_wrong))
        }

        Button(
            onClick = { onEvaluate(true) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text(stringResource(R.string.eval_correct))
        }
    }
}
