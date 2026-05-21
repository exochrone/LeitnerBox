package com.jb.leitnerbox.feature.dashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WorkspacePremium
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
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_decks)) },
                            leadingIcon = { Icon(Icons.Default.Style, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateToDecks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_history)) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateToHistory()
                            }
                        )
                        val challengeEnabled = uiState.masteredCardCount >= 2
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.menu_challenge),
                                    color = if (challengeEnabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (challengeEnabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            },
                            enabled = challengeEnabled,
                            onClick = {
                                showMenu = false
                                onNavigateToChallenge()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_settings)) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
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
