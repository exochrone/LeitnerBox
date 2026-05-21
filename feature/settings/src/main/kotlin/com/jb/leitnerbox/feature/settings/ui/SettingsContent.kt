package com.jb.leitnerbox.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onBackupClick: () -> Unit,
    onNotificationTimeClick: () -> Unit,
    onNewCardsPerDayChange: (Int) -> Unit,
    debugSection: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

        BackupEntry(onClick = onBackupClick)

        HorizontalDivider()

        NotificationTimeSection(
            notificationTime = uiState.notificationTime,
            onTimeClick = onNotificationTimeClick
        )

        HorizontalDivider()

        NewCardsPerDaySection(
            count = uiState.newCardsPerDay,
            onCountChange = onNewCardsPerDayChange
        )

        HorizontalDivider()
        
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text  = stringResource(R.string.settings_excluded_days_title),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text  = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text  = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text  = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun BackupEntry(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text  = stringResource(R.string.settings_backup_title),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text  = stringResource(R.string.settings_backup_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
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

@Composable
private fun NewCardsPerDaySection(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    var textValue by remember(count) {
        mutableStateOf(count.toString())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Activation quotidienne",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { c -> c.isDigit() }) {
                    textValue = newValue
                    newValue.toIntOrNull()?.let { n ->
                        if (n > 0) onCountChange(n)
                    }
                }
            },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            label = { Text("Nouvelles cartes par jour") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            text = "Nombre de nouvelles cartes activées automatiquement chaque jour.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
