package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<AppTheme> = repo.getTheme()
}
