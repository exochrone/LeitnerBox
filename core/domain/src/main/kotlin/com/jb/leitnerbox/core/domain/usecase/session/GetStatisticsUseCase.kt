package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class GlobalStatistics(
    val totalDecks: Int,
    val totalCards: Int,
    val learnedCards: Int,
    val cardsByBox: Map<Int, Int>
)

class GetStatisticsUseCase(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<GlobalStatistics> {
        return deckRepository.getDecks().flatMapLatest { decks ->
            if (decks.isEmpty()) {
                return@flatMapLatest flowOf(GlobalStatistics(0, 0, 0, emptyMap()))
            }
            
            val cardFlows = decks.map { cardRepository.getCardsByDeckId(it.id) }
            
            combine(cardFlows) { cardsLists ->
                val allCards = cardsLists.flatMap { it }
                val learnedCount = allCards.count { it.isLearned }
                val boxDistribution = allCards.groupBy { it.box }.mapValues { it.value.size }
                
                GlobalStatistics(
                    totalDecks = decks.size,
                    totalCards = allCards.size,
                    learnedCards = learnedCount,
                    cardsByBox = boxDistribution
                )
            }
        }
    }
}
