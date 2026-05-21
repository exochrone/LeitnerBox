package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.feature.dashboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSessionSelection: () -> Unit,
    onNavigateToDecks: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToChallenge: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) }
            )
        }
    ) { padding ->
        DashboardContent(
            uiState = uiState,
            onNavigateToSessionSelection = onNavigateToSessionSelection,
            onNavigateToDecks = onNavigateToDecks,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToChallenge = onNavigateToChallenge,
            onNavigateToSettings = onNavigateToSettings,
            onChallengeCardTrigger = viewModel::onChallengeCardClicked,
            onDismissDialog = viewModel::dismissChallengeDialog,
            modifier = Modifier.padding(padding)
        )
    }
}
