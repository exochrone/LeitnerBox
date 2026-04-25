package com.jb.leitnerbox.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jb.leitnerbox.feature.settings.BuildConfig
import com.jb.leitnerbox.feature.settings.R
import com.jb.leitnerbox.feature.settings.ui.debug.DebugSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsContent(
                uiState = uiState,
                onDayToggled = viewModel::onDayToggled,
                debugSection = {
                    if (BuildConfig.DEBUG) {
                        DebugSection()
                    }
                }
            )
        }
    }
}
