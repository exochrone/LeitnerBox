package com.jb.leitnerbox.feature.session.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SessionScreen(
    viewModel: SessionViewModel = hiltViewModel(),
    onSessionFinished: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SessionUiEvent.SessionFinished -> onSessionFinished()
            }
        }
    }

    SessionContent(
        uiState = uiState,
        onFlip = viewModel::onFlipCard,
        onEvaluate = viewModel::onEvaluate,
        onInputChanged = viewModel::onInputChanged,
        onInputValidated = viewModel::onInputValidated,
        onContinue = viewModel::onContinue,
        onBackClick = onBackClick
    )
}
