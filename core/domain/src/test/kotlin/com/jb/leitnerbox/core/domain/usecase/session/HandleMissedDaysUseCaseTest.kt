package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class HandleMissedDaysUseCaseTest {

    private val sessionRepository = mockk<SessionRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var useCase: HandleMissedDaysUseCase
    private val today = LocalDate.of(2026, 4, 12) // Sunday

    @BeforeEach
    fun setup() {
        useCase = HandleMissedDaysUseCase(sessionRepository, settingsRepository)
        every { settingsRepository.getExcludedDays() } returns flowOf(emptySet())
    }

    @Test
    fun `P5-UT-20 Aucune session en historique`() = runTest {
        every { sessionRepository.getSessions() } returns flowOf(emptyList())
        useCase(today)
        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `P5-UT-21 Dernière session hier`() = runTest {
        val yesterday = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(yesterday)
        ))
        useCase(today)
        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `P5-UT-22 Dernière session aujourd'hui`() = runTest {
        val todayInstant = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(todayInstant)
        ))
        useCase(today)
        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `P5-UT-23 Dernière session il y a 2 jours, aucun jour exclu`() = runTest {
        val twoDaysAgo = today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(twoDaysAgo)
        ))
        useCase(today)
        coVerify(exactly = 1) { sessionRepository.insertSession(match { it.isReported }) }
    }

    @Test
    fun `P5-UT-24 Dernière session il y a 3 jours, le jour intermédiaire est exclu`() = runTest {
        // Today = Sunday (4/12)
        // Last session = Thursday (4/9)
        // Friday (4/10) and Saturday (4/11) are gap days
        val lastSession = LocalDate.of(2026, 4, 9).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(lastSession)
        ))
        // Exclude Friday and Saturday
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))

        useCase(today)

        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `P5-UT-25 Dernière session il y a 3 jours, jours intermédiaires non exclus`() = runTest {
        val lastSession = LocalDate.of(2026, 4, 9).atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(lastSession)
        ))
        useCase(today)
        coVerify(exactly = 1) { sessionRepository.insertSession(match { it.isReported }) }
    }

    @Test
    fun `P5-UT-26 Session isReported déjà insérée aujourd'hui`() = runTest {
        val todayInstant = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            createMockSession(todayInstant, isReported = true)
        ))
        useCase(today)
        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    private fun createMockSession(date: java.time.Instant, isReported: Boolean = false) = Session(
        date = date,
        deckIds = emptyList(),
        cardCount = 0,
        successCount = 0,
        masteredCount = 0,
        isReported = isReported
    )
}
