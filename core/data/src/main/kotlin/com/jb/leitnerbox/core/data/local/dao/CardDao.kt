package com.jb.leitnerbox.core.data.local.dao

import androidx.room.*
import com.jb.leitnerbox.core.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsByDeckId(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: Long): Flow<CardEntity?>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND (nextReviewDate IS NULL OR nextReviewDate <= :now)")
    fun getCardsToReview(deckId: Long, now: Long): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND UPPER(recto) = UPPER(:recto) LIMIT 1")
    suspend fun getCardByRecto(deckId: Long, recto: String): CardEntity?
}
