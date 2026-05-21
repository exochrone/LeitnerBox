package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.usecase.tampon.ActivateBufferedCardsUseCase

class ActivateAllDecksCardsUseCase(
    private val cardRepository: CardRepository,
    private val activateCardsUseCase: ActivateBufferedCardsUseCase
) {
    suspend operator fun invoke() {
        val decksWithInactive = cardRepository.getDecksWithInactiveCards()
        decksWithInactive.forEach { deck ->
            activateCardsUseCase(deck.id)
        }
    }
}
