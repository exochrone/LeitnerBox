package com.jb.leitnerbox.feature.session.selection

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.feature.session.R

@Composable
fun SessionSelectionScreen(
    viewModel: SessionSelectionViewModel = hiltViewModel(),
    onStartSession: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SessionSelectionEvent.NavigateToSession -> onStartSession()
                is SessionSelectionEvent.ShowUndoPostpone -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.postpone_success,
                            event.deckName,
                            event.boxNumber
                        ),
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoPostpone(event.deckId, event.boxNumber, event.sessionId)
                    }
                }
            }
        }
    }

    SessionSelectionContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBoxToggled = viewModel::onBoxToggled,
        onPostponeBox = viewModel::onPostponeBox,
        onStartSession = viewModel::onStartSession,
        onBackClick = onBackClick
    )
}
