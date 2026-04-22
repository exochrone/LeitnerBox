package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow

class GetCardsUseCase(
    private val repository: CardRepository
) {
    operator fun invoke(deckId: Long): Flow<List<Card>> {
        return repository.getCardsByDeckId(deckId)
    }
}