package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository

class MoveCardsToBoxUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cards: List<Card>, targetBox: Int) {
        val updated = cards.map { it.copy(box = targetBox) }
        cardRepository.updateCards(updated)
    }
}
