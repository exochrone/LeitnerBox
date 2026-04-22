package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow

class GetCardByIdUseCase(
    private val repository: CardRepository
) {
    operator fun invoke(id: Long): Flow<Card?> {
        return repository.getCardById(id)
    }
}