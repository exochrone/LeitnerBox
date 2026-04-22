package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsByDeckId(deckId: Long): Flow<List<Card>>
    fun getCardById(id: Long): Flow<Card?>
    fun getCardsToReview(deckId: Long): Flow<List<Card>>
    suspend fun insertCard(card: Card): Long
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(card: Card)
    suspend fun getCardByRecto(deckId: Long, recto: String): Card?
}