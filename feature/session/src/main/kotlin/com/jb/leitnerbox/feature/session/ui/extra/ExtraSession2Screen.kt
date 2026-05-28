package com.jb.leitnerbox.feature.session.ui.extra

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.ui.components.TextToSpeechHelper
import com.jb.leitnerbox.feature.session.ui.Session2Content
import com.jb.leitnerbox.feature.session.ui.SessionUiState

@Composable
fun ExtraSession2Screen(
    viewModel: ExtraSessionViewModel = hiltViewModel(),
    onSessionFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }

    DisposableEffect(ttsHelper) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExtraSessionEvent.SessionFinished -> onSessionFinished()
                is ExtraSessionEvent.SpeakText -> ttsHelper.speak(event.text)
            }
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
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
            isChallenge = false,
            isTextToSpeechEnabled = uiState.isTextToSpeechEnabled
        )

        Session2Content(
            uiState = mappedState,
            onFlip = viewModel::onFlipCard,
            onEvaluate = viewModel::onEvaluate,
            onToggleTts = viewModel::toggleTextToSpeech,
            onSpeak = viewModel::onSpeakRequest,
            onZoomChange = viewModel::onZoomChange,
            onBackClick = onBackClick
        )
    }
}
