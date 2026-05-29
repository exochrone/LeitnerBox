package com.jb.leitnerbox.feature.session.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.core.ui.components.CelebrationOverlay
import com.jb.leitnerbox.core.ui.components.CelebrationType
import com.jb.leitnerbox.core.ui.components.TextToSpeechHelper

@Composable
fun Session2Screen(
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
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Session2Content(
            uiState = uiState,
            onFlip = viewModel::onToggleFlip,
            onEvaluate = viewModel::onEvaluate,
            onToggleTts = viewModel::toggleTextToSpeech,
            onToggleButtons = viewModel::onToggleButtons,
            onSpeak = viewModel::onSpeakRequest,
            onZoomChange = viewModel::onZoomChange,
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
