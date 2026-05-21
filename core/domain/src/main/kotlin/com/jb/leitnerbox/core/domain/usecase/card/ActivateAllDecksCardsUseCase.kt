package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.usecase.tampon.ActivateBufferedCardsUseCase

class ActivateAllDecksCardsUseCase(
    private val activateCardsUseCase: ActivateBufferedCardsUseCase
) {
    suspend operator fun invoke() {
        activateCardsUseCase()
    }
}
