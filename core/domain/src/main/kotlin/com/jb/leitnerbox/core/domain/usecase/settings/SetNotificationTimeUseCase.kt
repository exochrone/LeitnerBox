package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.LocalTime

class SetNotificationTimeUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(time: LocalTime) = repo.setNotificationTime(time)
}
