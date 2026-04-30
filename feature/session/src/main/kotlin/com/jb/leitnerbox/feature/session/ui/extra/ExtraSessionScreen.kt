package com.jb.leitnerbox.feature.session.ui.extra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.feature.session.ui.SessionContent
import com.jb.leitnerbox.feature.session.ui.SessionUiState

@Composable
fun ExtraSessionScreen(
    viewModel: ExtraSessionViewModel = hiltViewModel(),
    onSessionFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExtraSessionEvent.SessionFinished -> onSessionFinished()
            }
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Mapper ExtraSessionUiState vers SessionUiState pour réutiliser SessionContent
        val mappedState = SessionUiState(
            cards = uiState.cards,
            currentCard = uiState.currentCard,
            isFlipped = uiState.isFlipped,
            currentIndex = uiState.currentIndex,
            successCount = uiState.successCount,
            evaluatedCount = uiState.evaluatedCount,
            currentDeckName = uiState.currentDeckName,
            userInput = uiState.userInput,
            inputValidated = uiState.inputValidated,
            checkResult = uiState.checkResult,
            isChallenge = false // Les sessions extraordinaires n'affichent pas le mode challenge
        )

        SessionContent(
            uiState = mappedState,
            onFlip = viewModel::onFlipCard,
            onEvaluate = viewModel::onEvaluate,
            onInputChanged = viewModel::onInputChanged,
            onInputValidated = viewModel::onInputValidated,
            onContinue = viewModel::onContinue,
            onBackClick = onBackClick
        )
    }
}
