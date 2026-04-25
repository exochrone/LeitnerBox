package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.DayOfWeek

class SetExcludedDaysUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(days: Set<DayOfWeek>) = repo.setExcludedDays(days)
}
