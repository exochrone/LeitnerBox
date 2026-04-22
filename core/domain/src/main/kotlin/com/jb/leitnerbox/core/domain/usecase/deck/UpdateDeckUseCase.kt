package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository

class UpdateDeckUseCase(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(deck: Deck): Result<Unit> {
        if (deck.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Le nom du deck ne peut pas être vide"))
        }
        repository.updateDeck(deck)
        return Result.success(Unit)
    }
}