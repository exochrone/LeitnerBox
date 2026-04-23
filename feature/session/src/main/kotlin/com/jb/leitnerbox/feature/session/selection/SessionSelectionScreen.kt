package com.jb.leitnerbox.feature.session.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SessionSelectionScreen(
    viewModel: SessionSelectionViewModel = hiltViewModel(),
    onStartSession: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SessionSelectionContent(
        uiState = uiState,
        onBoxToggled = viewModel::onBoxToggled,
        onStartSession = onStartSession,
        onBackClick = onBackClick
    )
}
