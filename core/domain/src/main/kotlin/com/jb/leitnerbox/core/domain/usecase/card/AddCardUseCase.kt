package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import java.time.Instant

class AddCardUseCase(
    private val repository: CardRepository
) {
    suspend operator fun invoke(card: Card): Result<Long> {
        if (card.recto.isBlank() || card.verso.isBlank()) {
            return Result.failure(IllegalArgumentException("Recto and Verso cannot be empty"))
        }
        
        val rectoNormalized = AnswerNormalizer.normalize(card.recto)
        val existingCard = repository.getCardByRectoNormalized(card.deckId, rectoNormalized)
        if (existingCard != null) {
            return Result.failure(IllegalStateException("Une carte avec cette question existe déjà dans ce deck"))
        }

        val cardToInsert = card.copy(
            rectoNormalized = rectoNormalized,
            nextReviewDate = card.nextReviewDate ?: Instant.now(),
            answerNormalized = AnswerNormalizer.normalize(card.verso)
        )

        return Result.success(repository.insertCard(cardToInsert))
    }
}
