package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import java.time.Instant

class AddCardUseCase(
    private val repository: CardRepository
) {
    suspend operator fun invoke(card: Card): Result<Long> {
        if (card.recto.isBlank() || card.verso.isBlank()) {
            return Result.failure(IllegalArgumentException("Recto and Verso cannot be empty"))
        }
        
        val existingCard = repository.getCardByRecto(card.deckId, card.recto)
        if (existingCard != null) {
            return Result.failure(IllegalStateException("Une carte avec cette question existe déjà dans ce deck"))
        }

        val cardWithDate = if (card.nextReviewDate == null) {
            card.copy(nextReviewDate = Instant.now())
        } else {
            card
        }

        return Result.success(repository.insertCard(cardWithDate))
    }
}
