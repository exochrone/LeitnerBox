package com.jb.leitnerbox.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.feature.settings.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onDayToggled: (DayOfWeek) -> Unit,
    debugSection: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ExcludedDaysSection(
            excludedDays = uiState.excludedDays,
            onDayToggled = onDayToggled
        )

        // Other sections will be added in subsequent US
        
        debugSection()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExcludedDaysSection(
    excludedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_excluded_days_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in excludedDays,
                    onClick = { onDayToggled(day) },
                    label = { 
                        Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) 
                    }
                )
            }
        }
    }
}
