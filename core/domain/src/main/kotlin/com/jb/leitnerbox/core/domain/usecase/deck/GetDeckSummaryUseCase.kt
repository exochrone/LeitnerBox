package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class GetDeckSummaryUseCase(
    private val cardRepository: CardRepository
) {
    /**
     * Calcule en une seule passe sur les cartes du deck :
     * - La répartition par boîte (cartes en cours uniquement)
     * - Le nombre de cartes maîtrisées
     * - La date de prochaine révision (la plus proche parmi toutes les boîtes)
     * - La progression pondérée (approche 1 : score par position en boîte)
     *
     * Retourne un Flow<DeckSummary> qui se réémet à chaque modification des cartes.
     */
    operator fun invoke(deckId: Long, boxCount: Int): Flow<DeckSummary> =
        cardRepository.getCardsByDeckId(deckId).map { cards ->
            val masteredCards = cards.filter { it.isLearned }
            val inProgressCards = cards.filter { !it.isLearned }

            val cardsPerBox = inProgressCards
                .groupBy { it.box }
                .mapValues { it.value.size }

            val nextReviewDate = inProgressCards
                .mapNotNull { it.nextReviewDate }
                .minOrNull()

            val totalScore = cards.sumOf { card ->
                if (card.isLearned) boxCount.toDouble() else (card.box - 1).toDouble()
            }
            val progress = if (cards.isEmpty()) 0f
                           else (totalScore / (cards.size * boxCount)).toFloat()

            DeckSummary(
                masteredCount  = masteredCards.size,
                cardsPerBox    = cardsPerBox,
                nextReviewDate = nextReviewDate,
                progress       = progress
            )
        }
}
