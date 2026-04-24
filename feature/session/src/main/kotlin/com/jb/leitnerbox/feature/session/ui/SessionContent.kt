package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.ui.components.FlipCard
import com.jb.leitnerbox.feature.session.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionContent(
    uiState: SessionUiState,
    onFlip: () -> Unit,
    onEvaluate: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.session_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // TODO: Add back icon
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
                    FlipCard(
                        recto = card.recto,
                        verso = card.verso,
                        isFlipped = uiState.isFlipped,
                        onFlip = onFlip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 32.dp)
                    )
                }

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

@Composable
fun EvaluationButtons(
    onEvaluate: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onEvaluate(1) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.eval_hard))
        }
        Button(
            onClick = { onEvaluate(3) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.eval_correct))
        }
        Button(
            onClick = { onEvaluate(5) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text(stringResource(R.string.eval_easy))
        }
    }
}
