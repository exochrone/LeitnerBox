package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetGlobalSummaryUseCase(
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val deckRepository: DeckRepository
) {
    operator fun invoke(): Flow<GlobalSummary> = combine(
        cardRepository.getAllCards(),
        sessionRepository.getSessions(),
        deckRepository.getDecks()
    ) { cards, sessions, decks ->
        val completedSessions = sessions.filter { !it.isReported }
        val totalSuccess = completedSessions.sumOf { it.successCount }
        val totalCardsEvaluated = completedSessions.sumOf { it.cardCount }
        val globalSuccessRate = if (totalCardsEvaluated > 0)
            (totalSuccess * 100) / totalCardsEvaluated else null

        GlobalSummary(
            totalCards = cards.size,
            masteredCards = cards.count { it.isLearned },
            inProgressCards = cards.count { !it.isLearned },
            totalDecks = decks.size,
            totalSessions = completedSessions.size,
            globalSuccessRate = globalSuccessRate
        )
    }
}
