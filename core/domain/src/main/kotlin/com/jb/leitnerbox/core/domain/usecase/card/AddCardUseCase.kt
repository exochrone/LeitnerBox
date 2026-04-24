package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlinx.coroutines.flow.first
import java.time.Instant

class AddCardUseCase(
    private val repository: CardRepository,
    private val answerNormalizer: AnswerNormalizer
) {
    suspend operator fun invoke(card: Card): Result<Long> {
        if (card.recto.isBlank() || card.verso.isBlank()) {
            return Result.failure(IllegalArgumentException("Recto and Verso cannot be empty"))
        }
        
        val rectoNormalized = answerNormalizer.normalize(card.recto)
        val existingCard = repository.getCardByRectoNormalized(card.deckId, rectoNormalized)
        if (existingCard != null) {
            return Result.failure(IllegalStateException("Une carte avec cette question existe déjà dans ce deck"))
        }

        // Si la boîte a déjà un planning dans le futur (ex: suite à un report),
        // on aligne la nouvelle carte sur ce planning pour ne pas faire réapparaître la boîte aujourd'hui.
        val now = Instant.now()
        val cardsInBox = repository.getCardsByDeckId(card.deckId).first()
            .filter { it.box == card.box && !it.isLearned }
        
        val futureDate = if (cardsInBox.isNotEmpty() && cardsInBox.all { (it.nextReviewDate ?: Instant.MIN) > now }) {
            cardsInBox.mapNotNull { it.nextReviewDate }.minOrNull()
        } else {
            null
        }

        val cardToInsert = card.copy(
            rectoNormalized = rectoNormalized,
            nextReviewDate = card.nextReviewDate ?: futureDate ?: now,
            answerNormalized = answerNormalizer.normalize(card.verso)
        )

        return Result.success(repository.insertCard(cardToInsert))
    }
}
