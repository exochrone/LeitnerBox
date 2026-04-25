package com.jb.leitnerbox.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jb.leitnerbox.core.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY date DESC LIMIT 1")
    suspend fun getLastSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE date >= :startOfDay AND date <= :endOfDay LIMIT 1")
    suspend fun getSessionForDate(startOfDay: Long, endOfDay: Long): SessionEntity?

    @androidx.room.Delete
    suspend fun delete(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()
}
