package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.feature.dashboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartSession: () -> Unit,
    onStatsClick: () -> Unit,
    onChallengeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) })
        }
    ) { padding ->
        DashboardContent(
            uiState = uiState,
            onStartSession = onStartSession,
            onStatsClick = onStatsClick,
            onChallengeClick = onChallengeClick,
            modifier = Modifier.padding(padding)
        )
    }
}