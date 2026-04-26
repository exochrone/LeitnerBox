package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSessionHistoryUseCase(
    private val sessionRepository: SessionRepository,
    private val deckRepository: DeckRepository
) {
    operator fun invoke(): Flow<List<SessionHistoryItem>> = combine(
        sessionRepository.getSessions(),
        deckRepository.getDecks()
    ) { sessions, decks ->
        val deckMap = decks.associateBy { it.id }

        sessions
            .filter { !it.isReported && it.cardCount > 0 }
            .sortedByDescending { it.date }
            .map { session ->
                val deckNames = session.deckIds
                    .mapNotNull { deckMap[it]?.name }
                    .distinct()
                val successRate = if (session.cardCount > 0)
                    (session.successCount * 100) / session.cardCount else 0

                SessionHistoryItem(
                    session = session,
                    deckNames = deckNames,
                    successRate = successRate
                )
            }
    }
}
