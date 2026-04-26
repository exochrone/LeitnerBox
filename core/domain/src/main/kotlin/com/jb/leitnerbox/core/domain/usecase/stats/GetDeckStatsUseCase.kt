package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.ZoneId

class GetDeckStatsUseCase(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<List<DeckStat>> = combine(
        cardRepository.getAllCards(),
        deckRepository.getDecks(),
        sessionRepository.getSessions()
    ) { allCards, decks, sessions ->
        decks.map { deck ->
            val deckCards = allCards.filter { it.deckId == deck.id }
            val masteredCards = deckCards.count { it.isLearned }
            val inProgress = deckCards.filter { !it.isLearned }
            val cardsByBox = inProgress.groupBy { it.box }
                .mapValues { it.value.size }

            val deckSessions = sessions.filter {
                it.deckIds.contains(deck.id) && !it.isReported
            }
            val successRate = if (deckSessions.isEmpty()) null
            else {
                // Approximate success rate for this deck
                // (Note: session successCount is global for the session)
                val totalSuccess = deckSessions.sumOf { it.successCount }
                val totalCards = deckSessions.sumOf { it.cardCount }
                if (totalCards > 0) (totalSuccess * 100) / totalCards else null
            }

            val lastSessionDate = deckSessions.maxByOrNull { it.date }
                ?.date?.atZone(ZoneId.systemDefault())?.toLocalDate()

            DeckStat(
                deck = deck,
                totalCards = deckCards.size,
                masteredCards = masteredCards,
                cardsByBox = cardsByBox,
                successRate = successRate,
                lastSessionDate = lastSessionDate
            )
        }.sortedBy { it.deck.name }
    }
}
