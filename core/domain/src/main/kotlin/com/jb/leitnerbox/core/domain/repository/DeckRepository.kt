package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.DeckWithCardCount
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun getDecks(): Flow<List<Deck>>
    fun getDecksWithCardCount(): Flow<List<DeckWithCardCount>>
    fun getDeckById(id: Long): Flow<Deck?>
    suspend fun insertDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deck: Deck)
}