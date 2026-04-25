package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.repository.SettingsRepository

class SetThemeUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(theme: AppTheme) = repo.setTheme(theme)
}
