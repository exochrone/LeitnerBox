package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HandleMissedDaysUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()) {
        val sessions = sessionRepository.getSessions().first()

        // Aucune session précédente : rien à faire
        if (sessions.isEmpty()) return

        val lastSessionDate = sessions
            .maxBy { it.date }
            .date
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Dernière session aujourd'hui ou hier avec streak en cours : rien à faire
        val daysSinceLast = ChronoUnit.DAYS.between(lastSessionDate, today)
        if (daysSinceLast <= 1) return

        // Vérifier l'idempotence : une session isReported existe-t-elle déjà aujourd'hui ?
        val alreadyHandledToday = sessions.any { session ->
            session.isReported &&
            session.date.atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
        if (alreadyHandledToday) return

        // Insérer une session "absence" pour casser le streak
        sessionRepository.insertSession(
            Session(
                date = today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                deckIds = emptyList(),
                cardCount = 0,
                successCount = 0,
                masteredCount = 0,
                isReported = true
            )
        )
    }
}
