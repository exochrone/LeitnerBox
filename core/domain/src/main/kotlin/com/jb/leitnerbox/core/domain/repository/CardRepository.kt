package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsByDeckId(deckId: Long): Flow<List<Card>>
    fun getAllCards(): Flow<List<Card>>
    fun getMasteredCards(): Flow<List<Card>>
    fun getCardById(id: Long): Flow<Card?>
    fun getCardsToReview(deckId: Long): Flow<List<Card>>
    fun countGlobalActiveCardsInBoxOne(): Flow<Int>
    fun countActiveCardsInBoxOne(deckId: Long): Flow<Int>
    suspend fun getGlobalOldestInactiveCards(limit: Int): List<Card>
    suspend fun getOldestInactiveCards(deckId: Long, limit: Int): List<Card>
    fun observeInactiveCardsCount(deckId: Long): Flow<Int>
    suspend fun getDecksWithInactiveCards(): List<com.jb.leitnerbox.core.domain.model.Deck>
    suspend fun insertCard(card: Card): Long
    suspend fun insertCards(cards: List<Card>): List<Long>
    suspend fun insertCardWithId(card: Card)
    suspend fun insertCardsWithId(cards: List<Card>)
    suspend fun updateCard(card: Card)
    suspend fun updateCards(cards: List<Card>)
    suspend fun deleteCard(card: Card)
    suspend fun getCardByRectoNormalized(deckId: Long, rectoNormalized: String): Card?
}
