package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

class GetExcludedDaysUseCase(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<Set<DayOfWeek>> = repo.getExcludedDays()
}
