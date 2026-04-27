package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.Session
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SessionRepository {
    fun getSessions(): Flow<List<Session>>
    suspend fun insertSession(session: Session): Long
    suspend fun insertSessionWithId(session: Session)
    suspend fun deleteSession(session: Session)
    suspend fun getLastSession(): Session?
    suspend fun getSessionForDate(date: LocalDate): Session?
    suspend fun deleteAllSessions()
}
