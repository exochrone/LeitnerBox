package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow

class GetDeckByIdUseCase(
    private val repository: DeckRepository
) {
    operator fun invoke(id: Long): Flow<Deck?> {
        return repository.getDeckById(id)
    }
}
