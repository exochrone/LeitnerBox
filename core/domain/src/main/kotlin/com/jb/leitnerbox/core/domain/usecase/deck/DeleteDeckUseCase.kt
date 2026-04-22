package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository

class DeleteDeckUseCase(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(deck: Deck) {
        repository.deleteDeck(deck)
    }
}