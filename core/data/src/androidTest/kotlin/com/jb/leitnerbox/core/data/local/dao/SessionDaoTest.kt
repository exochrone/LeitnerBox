package com.jb.leitnerbox.core.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jb.leitnerbox.core.data.local.LeitnerDatabase
import com.jb.leitnerbox.core.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var database: LeitnerDatabase
    private lateinit var sessionDao: SessionDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LeitnerDatabase::class.java
        ).allowMainThreadQueries().build()
        sessionDao = database.sessionDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetLastSession() = runBlocking {
        val session = SessionEntity(
            date = Instant.now().minusSeconds(3600),
            deckIds = listOf(1L),
            cardCount = 10,
            successCount = 8,
            masteredCount = 2,
            advancedCount = 5,
            retreatedCount = 1,
            isReported = false
        )
        sessionDao.insert(session)

        val lastSession = sessionDao.getLastSession()
        assertNotNull(lastSession)
        assertEquals(10, lastSession?.cardCount)
    }

    @Test
    fun getSessionForDate() = runBlocking {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val session = SessionEntity(
            date = Instant.now(),
            deckIds = listOf(1L),
            cardCount = 5,
            successCount = 5,
            masteredCount = 1,
            advancedCount = 4,
            retreatedCount = 0,
            isReported = true
        )
        sessionDao.insert(session)

        val sessionForDate = sessionDao.getSessionForDate(startOfDay, endOfDay)
        assertNotNull(sessionForDate)
        assertEquals(true, sessionForDate?.isReported)
    }

    @Test
    fun getAllSessions_returnsSortedByDateDesc() = runBlocking {
        val now = Instant.now()
        sessionDao.insert(SessionEntity(date = now.minusSeconds(100), deckIds = listOf(1L), cardCount = 1, successCount = 1, masteredCount = 0, advancedCount = 1, retreatedCount = 0, isReported = false))
        sessionDao.insert(SessionEntity(date = now, deckIds = listOf(1L), cardCount = 2, successCount = 2, masteredCount = 0, advancedCount = 2, retreatedCount = 0, isReported = false))

        val sessions = sessionDao.getAllSessions().first()
        assertEquals(2, sessions.size)
        assertEquals(2, sessions[0].cardCount) // Le plus récent en premier
    }
}
