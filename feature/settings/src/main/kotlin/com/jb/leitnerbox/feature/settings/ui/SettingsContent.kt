package com.jb.leitnerbox.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.feature.settings.R
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.*

@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onExcludedDaysClick: () -> Unit,
    onThemeClick: () -> Unit,
    onNotificationTimeClick: () -> Unit,
    backupSection: @Composable () -> Unit = {},
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
        ExcludedDaysEntry(
            excludedDays = uiState.excludedDays,
            onClick      = onExcludedDaysClick
        )

        HorizontalDivider()

        ThemeEntry(
            currentTheme = uiState.theme,
            onClick      = onThemeClick
        )

        HorizontalDivider()

        NotificationTimeSection(
            notificationTime = uiState.notificationTime,
            onTimeClick = onNotificationTimeClick
        )

        HorizontalDivider()

        backupSection()
        
        debugSection()
    }
}

@Composable
internal fun ExcludedDaysEntry(
    excludedDays: Set<DayOfWeek>,
    onClick: () -> Unit
) {
    val summary = when {
        excludedDays.isEmpty() -> stringResource(R.string.excluded_days_none)
        excludedDays.size == 7 -> stringResource(R.string.excluded_days_all)
        else -> excludedDays
            .sortedBy { it.value }
            .joinToString(", ") { day ->
                day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    .replaceFirstChar { it.uppercase() }
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = stringResource(R.string.settings_excluded_days_title),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ThemeEntry(
    currentTheme: AppTheme,
    onClick: () -> Unit
) {
    val summary = stringResource(
        when (currentTheme) {
            AppTheme.SYSTEM -> R.string.settings_theme_system
            AppTheme.LIGHT  -> R.string.settings_theme_light
            AppTheme.DARK   -> R.string.settings_theme_dark
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
internal fun BackupSection(
    isLoading: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.backup_section_title),
            style = MaterialTheme.typography.titleSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onExport,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.backup_export_button))
            }
            OutlinedButton(
                onClick = onImport,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.backup_import_button))
            }
        }
    }
}

@Composable
private fun NotificationTimeSection(
    notificationTime: LocalTime,
    onTimeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_notification_time),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            onClick = onTimeClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_notification_label),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = notificationTime.format(
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
