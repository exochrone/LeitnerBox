package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository

class UpdateDeckColorUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(deck: Deck, colorHex: String): Deck {
        val updated = deck.copy(color = colorHex)
        deckRepository.updateDeck(updated)
        return updated
    }
}
