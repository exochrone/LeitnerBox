package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository

class InsertCardsUseCase(
    private val cardRepository: CardRepository
) {
    /**
     * Insère une liste de cartes en préservant leurs IDs d'origine.
     * Utilisé uniquement pour la restauration après annulation de suppression.
     */
    suspend operator fun invoke(cards: List<Card>) {
        if (cards.isEmpty()) return
        cardRepository.insertCardsWithId(cards)
    }
}
