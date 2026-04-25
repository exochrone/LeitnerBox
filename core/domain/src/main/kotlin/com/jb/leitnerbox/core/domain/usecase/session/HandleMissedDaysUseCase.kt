package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HandleMissedDaysUseCase(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()) {
        val sessions = sessionRepository.getSessions().first()
        if (sessions.isEmpty()) return

        val lastSessionDate = sessions
            .maxBy { it.date }
            .date.atZone(ZoneId.systemDefault()).toLocalDate()

        val daysSinceLast = ChronoUnit.DAYS.between(lastSessionDate, today)
        if (daysSinceLast <= 1) return

        // Vérifier si le gap ne contient que des jours exclus
        val excludedDays = settingsRepository.getExcludedDays().first()
        val gapDays = (1 until daysSinceLast).map {
            lastSessionDate.plusDays(it).dayOfWeek
        }
        if (gapDays.all { it in excludedDays }) return

        // Idempotence : ne pas insérer deux fois
        val alreadyHandled = sessions.any { session ->
            session.isReported &&
            session.date.atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
        if (alreadyHandled) return

        sessionRepository.insertSession(
            Session(
                date = today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                deckIds = emptyList(),
                cardCount = 0,
                successCount = 0,
                masteredCount = 0,
                advancedCount = 0,
                retreatedCount = 0,
                isReported = true
            )
        )
    }
}
