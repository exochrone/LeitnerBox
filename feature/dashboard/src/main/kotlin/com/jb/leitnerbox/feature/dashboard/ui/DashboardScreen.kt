package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
    var menuExpanded by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded        = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text(stringResource(R.string.menu_decks)) },
                            onClick = { menuExpanded = false; onNavigateToDecks() }
                        )
                        DropdownMenuItem(
                            text    = { Text(stringResource(R.string.menu_history)) },
                            onClick = { menuExpanded = false; onNavigateToHistory() }
                        )
                        DropdownMenuItem(
                            text    = {
                                Text(
                                    text  = stringResource(R.string.menu_challenge),
                                    color = if (uiState.masteredCardCount >= 2)
                                        LocalContentColor.current
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            },
                            onClick = {
                                if (uiState.masteredCardCount >= 2) {
                                    menuExpanded = false
                                    onNavigateToChallenge()
                                }
                            },
                            enabled = uiState.masteredCardCount >= 2
                        )
                        DropdownMenuItem(
                            text    = { Text(stringResource(R.string.menu_settings)) },
                            onClick = { menuExpanded = false; onNavigateToSettings() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        DashboardContent(
            uiState = uiState,
            onNavigateToSessionSelection = onNavigateToSessionSelection,
            onNavigateToDecks = onNavigateToDecks,
            modifier = Modifier.padding(padding)
        )
    }
}
