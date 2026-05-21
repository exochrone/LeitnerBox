package com.jb.leitnerbox.core.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class AppSettings(
    val excludedDays: Set<DayOfWeek>,
    val notificationTime: LocalTime,
    val theme: AppTheme,
    val maxDailyNewCards: Int,
    val cardsActivatedToday: Int,
    val lastActivationDateIso: String?
)
