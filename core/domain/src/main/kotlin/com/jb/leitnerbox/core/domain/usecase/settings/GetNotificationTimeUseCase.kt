package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

class GetNotificationTimeUseCase(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<LocalTime> = repo.getNotificationTime()
}
