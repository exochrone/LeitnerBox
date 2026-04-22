package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository

class DeleteCardUseCase(
    private val repository: CardRepository
) {
    suspend operator fun invoke(card: Card) {
        repository.deleteCard(card)
    }
}
