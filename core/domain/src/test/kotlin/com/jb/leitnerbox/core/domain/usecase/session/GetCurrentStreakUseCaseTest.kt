package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class GetCurrentStreakUseCaseTest {

    private val sessionRepository = mockk<SessionRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var useCase: GetCurrentStreakUseCase
    private val sunday = LocalDate.of(2026, 4, 12)
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        useCase = GetCurrentStreakUseCase(sessionRepository, settingsRepository)
        every { settingsRepository.getExcludedDays() } returns flowOf(emptySet())
    }

    @Test
    fun `P5-UT-01 Aucune session en historique`() = runTest(testDispatcher) {
        every { sessionRepository.getSessions() } returns flowOf(emptyList())
        val streak = useCase(sunday).first()
        assertEquals(0, streak)
    }

    @Test
    fun `P5-UT-02 Une session completee aujourd'hui`() = runTest(testDispatcher) {
        val todayInstant = sunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(todayInstant)
        ))
        val streak = useCase(sunday).first()
        assertEquals(1, streak)
    }

    @Test
    fun `P5-UT-03 Sessions completees 3 jours consecutifs`() = runTest(testDispatcher) {
        val d1 = sunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val d2 = sunday.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val d3 = sunday.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(d1), createMockSession(d2), createMockSession(d3)
        ))
        val streak = useCase(sunday).first()
        assertEquals(3, streak)
    }

    @Test
    fun `P5-UT-04 Session reportee aujourd'hui`() = runTest(testDispatcher) {
        val todayInstant = sunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(todayInstant, isReported = true)
        ))
        val streak = useCase(sunday).first()
        assertEquals(0, streak)
    }

    @Test
    fun `P5-UT-05 Session completee ET session reportee le meme jour`() = runTest(testDispatcher) {
        val todayInstant = sunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(todayInstant, isReported = false),
            createMockSession(todayInstant, isReported = true)
        ))
        val streak = useCase(sunday).first()
        assertEquals(0, streak)
    }

    @Test
    fun `P5-UT-06 Session completee hier, aucune session aujourd'hui`() = runTest(testDispatcher) {
        val yesterday = sunday.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(yesterday)
        ))
        val streak = useCase(sunday).first()
        assertEquals(0, streak)
    }

    @Test
    fun `P5-UT-07 Session vendredi, samedi+dimanche exclus, session lundi`() = runTest(testDispatcher) {
        val monday = LocalDate.of(2026, 4, 13)
        val friday = LocalDate.of(2026, 4, 10)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(monday.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            createMockSession(friday.atStartOfDay(ZoneId.systemDefault()).toInstant())
        ))
        
        val streak = useCase(monday).first()
        assertEquals(2, streak)
    }

    @Test
    fun `P5-UT-08 Session vendredi, samedi+dimanche NON exclus, session lundi`() = runTest(testDispatcher) {
        val monday = LocalDate.of(2026, 4, 13)
        val friday = LocalDate.of(2026, 4, 10)
        
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(monday.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            createMockSession(friday.atStartOfDay(ZoneId.systemDefault()).toInstant())
        ))
        
        val streak = useCase(monday).first()
        assertEquals(1, streak)
    }

    @Test
    fun `P5-UT-09 Mercredi sans session planifiee, session mardi et jeudi`() = runTest(testDispatcher) {
        val thursday = LocalDate.of(2026, 4, 16)
        val tuesday = LocalDate.of(2026, 4, 14)
        
        // Mercredi est neutre car exclu dans ce test pour satisfaire la regle
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.WEDNESDAY))
        
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(thursday.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            createMockSession(tuesday.atStartOfDay(ZoneId.systemDefault()).toInstant())
        ))
        
        val streak = useCase(thursday).first()
        assertEquals(2, streak)
    }

    @Test
    fun `P5-UT-10 Session reportee il y a 2 jours, session completee hier`() = runTest(testDispatcher) {
        val yesterday = sunday.minusDays(1)
        val twoDaysAgo = sunday.minusDays(2)
        
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant(), isReported = false),
            createMockSession(twoDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant(), isReported = true)
        ))
        
        val streak = useCase(sunday).first()
        assertEquals(0, streak) 
    }

    @Test
    fun `P5-UT-11 Streak de 5 jours puis jour manque puis 2 jours de suite`() = runTest(testDispatcher) {
        val sessions = mutableListOf<Session>()
        sessions.add(createMockSession(sunday.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        sessions.add(createMockSession(sunday.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
        // reported gap at -2
        sessions.add(createMockSession(sunday.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant(), isReported = true))
        for (i in 3..7) {
            sessions.add(createMockSession(sunday.minusDays(i.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant()))
        }
        
        every { sessionRepository.getSessions() } returns flowOf(sessions)
        
        val streak = useCase(sunday).first()
        assertEquals(2, streak)
    }

    private fun createMockSession(date: java.time.Instant, isReported: Boolean = false) = Session(
        date = date,
        deckIds = emptyList(),
        cardCount = 0,
        successCount = 0,
        masteredCount = 0,
        advancedCount = 0,
        retreatedCount = 0,
        isReported = isReported
    )
}
