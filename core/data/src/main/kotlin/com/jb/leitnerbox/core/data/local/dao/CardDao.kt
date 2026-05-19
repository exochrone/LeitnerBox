package com.jb.leitnerbox.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jb.leitnerbox.core.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsByDeckId(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isLearned = 1")
    fun getMasteredCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: Long): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND isActive = 1 AND (nextReviewDate IS NULL OR nextReviewDate <= :now)")
    fun getCardsToReview(deckId: Long, now: Long): Flow<List<CardEntity>>

    @Query("""
        SELECT * FROM cards 
        WHERE deckId = :deckId AND isActive = 0 
        ORDER BY importOrder ASC 
        LIMIT :limit
    """)
    suspend fun getInactiveCards(deckId: Long, limit: Int): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND isActive = 0")
    fun observeInactiveCardsCount(deckId: Long): Flow<Int>

    @Query("SELECT * FROM decks WHERE id IN (SELECT DISTINCT deckId FROM cards WHERE isActive = 0)")
    suspend fun getDecksWithInactiveCards(): List<com.jb.leitnerbox.core.data.local.entity.DeckEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCards(cards: List<CardEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardWithId(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardsWithId(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND rectoNormalized = :rectoNormalized LIMIT 1")
    suspend fun getCardByRectoNormalized(deckId: Long, rectoNormalized: String): CardEntity?
}
