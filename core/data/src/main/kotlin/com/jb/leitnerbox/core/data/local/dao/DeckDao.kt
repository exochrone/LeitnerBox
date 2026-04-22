package com.jb.leitnerbox.core.data.local.dao

import androidx.room.*
import com.jb.leitnerbox.core.data.local.entity.DeckEntity
import com.jb.leitnerbox.core.data.local.entity.DeckWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("""
        SELECT decks.*, (SELECT COUNT(*) FROM cards WHERE cards.deckId = decks.id) as cardCount 
        FROM decks 
        ORDER BY name ASC
    """)
    fun getDecksWithCardCount(): Flow<List<DeckWithCount>>

    @Query("SELECT * FROM decks")
    fun getDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :id")
    fun getDeckById(id: Long): Flow<DeckEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)
}
