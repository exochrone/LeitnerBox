package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer

class UpdateCardUseCase(
    private val repository: CardRepository,
    private val answerNormalizer: AnswerNormalizer
) {
    suspend operator fun invoke(card: Card): Result<Unit> {
        if (card.recto.isBlank() || card.verso.isBlank()) {
            return Result.failure(IllegalArgumentException("Le recto et le verso ne peuvent pas être vides"))
        }

        val cardToUpdate = card.copy(
            rectoNormalized = answerNormalizer.normalize(card.recto),
            answerNormalized = answerNormalizer.normalize(card.verso)
        )

        repository.updateCard(cardToUpdate)
        return Result.success(Unit)
    }
}
