package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.ui.components.CelebrationOverlay
import com.jb.leitnerbox.core.ui.components.CelebrationType
import com.jb.leitnerbox.core.ui.components.TextToSpeechHelper

@Composable
fun SessionScreen(
    viewModel: SessionViewModel = hiltViewModel(),
    onSessionFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCelebration by remember { mutableStateOf<CelebrationType?>(null) }
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
                is SessionUiEvent.SessionFinished -> onSessionFinished()
                is SessionUiEvent.CardMastered -> showCelebration = CelebrationType.CARD_MASTERED
                is SessionUiEvent.SpeakText -> ttsHelper.speak(event.text)
                is SessionUiEvent.NavigateBack -> onBackClick()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SessionContent(
            uiState = uiState,
            onFlip = viewModel::onFlipCard,
            onEvaluate = viewModel::onEvaluate,
            onInputChanged = viewModel::onInputChanged,
            onInputValidated = viewModel::onInputValidated,
            onContinue = viewModel::onContinue,
            onSpeak = viewModel::onSpeakRequest,
            onToggleTts = viewModel::toggleTextToSpeech,
            onBackClick = onBackClick
        )

        showCelebration?.let { type ->
            CelebrationOverlay(
                type = type,
                onFinished = {
                    showCelebration = null
                    if (type == CelebrationType.CARD_MASTERED) {
                        viewModel.onMasteryCelebrationFinished()
                    }
                }
            )
        }
    }
}
