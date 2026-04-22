package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository

class UpdateCardUseCase(
    private val repository: CardRepository
) {
    suspend operator fun invoke(card: Card): Result<Unit> {
        if (card.recto.isBlank() || card.verso.isBlank()) {
            return Result.failure(IllegalArgumentException("Le recto et le verso ne peuvent pas être vides"))
        }
        repository.updateCard(card)
        return Result.success(Unit)
    }
}