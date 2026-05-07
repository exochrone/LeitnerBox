package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository

class RestoreDeckUseCase(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(deck: Deck): Result<Long> {
        return try {
            val id = repository.insertDeckWithId(deck)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
