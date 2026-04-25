package com.jb.leitnerbox.core.data.repository

import com.jb.leitnerbox.core.data.local.dao.SessionDao
import com.jb.leitnerbox.core.data.local.entity.toDomain
import com.jb.leitnerbox.core.data.local.entity.toEntity
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
    override fun getSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSession(session: Session): Long {
        return sessionDao.insert(session.toEntity())
    }

    override suspend fun deleteSession(session: Session) {
        sessionDao.delete(session.toEntity())
    }

    override suspend fun getLastSession(): Session? {
        return sessionDao.getLastSession()?.toDomain()
    }

    override suspend fun getSessionForDate(date: LocalDate): Session? {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return sessionDao.getSessionForDate(startOfDay, endOfDay)?.toDomain()
    }

    override suspend fun deleteAllSessions() {
        sessionDao.deleteAllSessions()
    }
}
