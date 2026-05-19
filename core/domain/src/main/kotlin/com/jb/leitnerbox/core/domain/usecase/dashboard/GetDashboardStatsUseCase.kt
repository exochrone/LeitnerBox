package com.jb.leitnerbox.core.domain.usecase.dashboard

import com.jb.leitnerbox.core.domain.model.DashboardGlobalStats
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.usecase.session.GetCurrentStreakUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDashboardStatsUseCase(
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val deckRepository: DeckRepository,
    private val getCurrentStreak: GetCurrentStreakUseCase
) {
    operator fun invoke(): Flow<DashboardGlobalStats> = combine(
        cardRepository.getAllCards(),
        sessionRepository.getSessions(),
        deckRepository.getDecks(),
        getCurrentStreak()
    ) { cards, sessions, decks, streak ->

        val completedSessions = sessions.filter { !it.isReported }

        // Taux de bonnes réponses global
        val totalCardCount   = completedSessions.sumOf { it.cardCount }
        val totalSuccessCount = completedSessions.sumOf { it.successCount }
        val successRate = if (totalCardCount > 0)
            (totalSuccessCount * 100) / totalCardCount else null

        // OPTIMISATION : Map d'accès direct O(1) pour associer deckId -> taille des intervalles
        val defaultBoxCount = 5
        val deckIntervalsSizeMap = decks.associate { it.id to it.intervals.size }

        // Progression globale linéaire optimisée O(N)
        val progressionGlobal = if (cards.isEmpty()) 0 else {
            var totalScore = 0.0
            var maxScore = 0.0

            cards.forEach { card ->
                val bc = deckIntervalsSizeMap[card.deckId] ?: defaultBoxCount
                totalScore += if (card.isLearned) bc.toDouble() else (card.box - 1).toDouble()
                maxScore += bc.toDouble()
            }

            if (maxScore > 0) ((totalScore / maxScore) * 100).toInt() else 0
        }

        DashboardGlobalStats(
            streak            = streak,
            totalCards        = cards.size,
            masteredCards     = cards.count { it.isLearned },
            successRate       = successRate,
            progressionGlobal = progressionGlobal,
            sessionCount      = completedSessions.size,
            deckCount         = decks.size
        )
    }
}
