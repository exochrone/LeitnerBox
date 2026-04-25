package com.jb.leitnerbox.feature.settings.ui

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val time = uiState.notificationTime
        TimePickerDialog(
            context,
            { _, hour, minute -> 
                viewModel.onNotificationTimeSelected(hour, minute)
                showTimePicker = false
            },
            time.hour,
            time.minute,
            DateFormat.is24HourFormat(context)
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsContent(
                uiState = uiState,
                onDayToggled = viewModel::onDayToggled,
                onThemeSelected = viewModel::onThemeSelected,
                onNotificationTimeClick = { showTimePicker = true },
                debugSection = {
                    if (BuildConfig.DEBUG) {
                        DebugSection()
                    }
                }
            )
        }
    }
}
