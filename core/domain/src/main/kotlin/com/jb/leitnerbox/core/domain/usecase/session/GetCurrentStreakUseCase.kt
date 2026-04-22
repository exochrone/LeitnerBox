package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class GetCurrentStreakUseCase(
    private val repository: SessionRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getSessions().map { sessions ->
            if (sessions.isEmpty()) return@map 0

            // Trier par date décroissante
            val sortedSessions = sessions.sortedByDescending { it.date }
            
            var streak = 0
            var lastDate: LocalDate? = null
            
            for (session in sortedSessions) {
                if (session.isReported) break
                
                val sessionDate = session.date.atZone(ZoneId.systemDefault()).toLocalDate()
                
                if (lastDate == null) {
                    // Vérifier si la session la plus récente est aujourd'hui ou hier
                    val today = LocalDate.now()
                    if (sessionDate == today || sessionDate == today.minusDays(1)) {
                        streak++
                        lastDate = sessionDate
                    } else {
                        // Trop vieux pour un streak actuel
                        break
                    }
                } else {
                    if (sessionDate == lastDate.minusDays(1)) {
                        streak++
                        lastDate = sessionDate
                    } else if (sessionDate == lastDate) {
                        // Plusieurs sessions le même jour, on continue sans incrémenter le streak
                        continue
                    } else {
                        // Trou dans la continuité
                        break
                    }
                }
            }
            streak
        }
    }
}
