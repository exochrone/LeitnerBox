package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessions(): Flow<List<Session>>
    suspend fun insertSession(session: Session): Long
    suspend fun deleteSession(session: Session)
    
    /**
     * Calcule le streak actuel : nombre de sessions consécutives sans report.
     * Dès qu'une session est reportée, le streak tombe à zéro.
     */
    fun getCurrentStreak(): Flow<Int>
}