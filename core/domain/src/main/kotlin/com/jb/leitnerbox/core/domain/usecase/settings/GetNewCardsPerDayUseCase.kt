package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetNewCardsPerDayUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Int> = repository.getNewCardsPerDay()
}
