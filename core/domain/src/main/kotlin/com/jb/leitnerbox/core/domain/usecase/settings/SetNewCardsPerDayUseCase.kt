package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository

class SetNewCardsPerDayUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(count: Int) = repository.setNewCardsPerDay(count)
}
