package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetCurrentStreakUseCase(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(referenceDate: LocalDate = LocalDate.now(ZoneId.systemDefault())): Flow<Int> = combine(
        sessionRepository.getSessions(),
        settingsRepository.getExcludedDays()
    ) { sessions, excludedDays ->

        // Dates ayant au moins un report → invalident toute la journée
        val reportedDates = sessions
            .filter { it.isReported }
            .map { it.date.atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSet()

        // Jours valides = au moins une session complétée ET aucun report ce jour-là
        val validDates = sessions
            .filter { !it.isReported }
            .map { it.date.atZone(ZoneId.systemDefault()).toLocalDate() }
            .filter { it !in reportedDates }
            .distinct()
            .sortedDescending()

        if (validDates.isEmpty()) return@combine 0

        val today = referenceDate
        val mostRecent = validDates.first()
        val daysSinceMostRecent = ChronoUnit.DAYS.between(mostRecent, today)

        // 1. Vérifier si le streak est toujours actif (gap vers aujourd'hui)
        if (daysSinceMostRecent > 0) {
            val gapToToday = (0 until daysSinceMostRecent).map {
                mostRecent.plusDays(it.toLong() + 1)
            }
            // Si le gap contient un jour non exclu (y compris aujourd'hui), le streak est brisé.
            // Sauf si on a fait une session aujourd'hui (mais ici daysSinceMostRecent > 0 donc non)
            if (!gapToToday.all { it.dayOfWeek in excludedDays }) return@combine 0
        }

        // 2. Compter les jours valides consécutifs en remontant l'historique
        var streak = 1
        for (i in 0 until validDates.size - 1) {
            val current  = validDates[i]
            val previous = validDates[i + 1]
            val gap = ChronoUnit.DAYS.between(previous, current)

            if (gap == 1L) {
                // Jours consécutifs normaux
                streak++
            } else {
                // Vérifier si le gap est entièrement composé de jours exclus
                val gapDays = (1 until gap).map {
                    previous.plusDays(it).dayOfWeek
                }
                if (gapDays.all { it in excludedDays }) {
                    streak++
                } else {
                    break
                }
            }
        }
        streak
    }
}
