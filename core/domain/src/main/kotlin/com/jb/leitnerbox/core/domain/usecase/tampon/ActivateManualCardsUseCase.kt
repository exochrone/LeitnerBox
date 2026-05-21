package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.repository.CardRepository
import java.time.Instant

class ActivateManualCardsUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(deckId: Long, count: Int) {
        if (count <= 0) return
        
        val cardsToActivate = cardRepository.getOldestInactiveCards(deckId, count)
        
        val updatedCards = cardsToActivate.map { card ->
            card.copy(
                isActive = true,
                box = 1,
                nextReviewDate = Instant.now()
            )
        }
        
        cardRepository.updateCards(updatedCards)
    }
}
