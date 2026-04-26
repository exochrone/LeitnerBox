package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDeckProgressUseCase(
    private val cardRepository: CardRepository
) {
    /**
     * Retourne un Flow<Float> entre 0f (0 %) et 1f (100 %).
     * Se réémet automatiquement à chaque modification des cartes du deck.
     * Retourne 0f si le deck est vide.
     *
     * Formule :
     *   score_carte = (box - 1) / boxCount          (carte en cours)
     *   score_carte = 1.0                            (carte maîtrisée)
     *   progression = SOMME(score_carte) / nb_cartes
     */
    operator fun invoke(deckId: Long, boxCount: Int): Flow<Float> =
        cardRepository.getCardsByDeckId(deckId).map { cards ->
            if (cards.isEmpty()) return@map 0f
            val totalScore = cards.sumOf { card ->
                when {
                    card.isLearned -> boxCount.toDouble()
                    else           -> (card.box - 1).toDouble()
                }
            }
            val maxScore = cards.size.toDouble() * boxCount
            (totalScore / maxScore).toFloat()
        }
}
