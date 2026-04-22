package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.DeckWithCardCount
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow

class GetDecksUseCase(
    private val repository: DeckRepository
) {
    operator fun invoke(): Flow<List<Deck>> {
        return repository.getDecks()
    }

    fun executeWithCardCount(): Flow<List<DeckWithCardCount>> {
        return repository.getDecksWithCardCount()
    }
}
