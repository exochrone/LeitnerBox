package com.jb.leitnerbox.feature.settings.ui

import com.jb.leitnerbox.core.domain.model.AppTheme
import java.time.DayOfWeek
import java.time.LocalTime

data class SettingsUiState(
    val excludedDays: Set<DayOfWeek> = emptySet(),
    val theme: AppTheme = AppTheme.SYSTEM,
    val notificationTime: LocalTime = LocalTime.of(20, 0)
)
