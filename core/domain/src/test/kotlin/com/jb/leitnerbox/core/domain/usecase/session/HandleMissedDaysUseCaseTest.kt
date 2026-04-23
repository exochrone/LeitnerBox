package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class HandleMissedDaysUseCaseTest {

    private val sessionRepository = mockk<SessionRepository>(relaxed = true)
    private val useCase = HandleMissedDaysUseCase(sessionRepository)
    private val today = LocalDate.of(2026, 4, 12)

    @Test
    fun `no action when no previous session exists`() = runTest {
        every { sessionRepository.getSessions() } returns flowOf(emptyList())

        useCase(today)

        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `no action when last session was yesterday`() = runTest {
        val yesterday = today.minusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(
            listOf(Session(date = yesterday, deckIds = emptyList(),
                cardCount = 0, successCount = 0, masteredCount = 0,
                isReported = false))
        )

        useCase(today)

        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `inserts reported session when last session was 2 days ago`() = runTest {
        val twoDaysAgo = today.minusDays(2)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(
            listOf(Session(date = twoDaysAgo, deckIds = emptyList(),
                cardCount = 0, successCount = 0, masteredCount = 0,
                isReported = false))
        )

        useCase(today)

        coVerify(exactly = 1) {
            sessionRepository.insertSession(match { it.isReported && it.cardCount == 0 })
        }
    }

    @Test
    fun `idempotent - no double insertion if reported session already exists today`() = runTest {
        val twoDaysAgo = today.minusDays(2)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        val todayInstant = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(listOf(
            Session(date = twoDaysAgo, deckIds = emptyList(),
                cardCount = 0, successCount = 0, masteredCount = 0,
                isReported = false),
            Session(date = todayInstant, deckIds = emptyList(),
                cardCount = 0, successCount = 0, masteredCount = 0,
                isReported = true)
        ))

        useCase(today)

        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }

    @Test
    fun `no action when last session was today`() = runTest {
        val todayInstant = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        every { sessionRepository.getSessions() } returns flowOf(
            listOf(Session(date = todayInstant, deckIds = emptyList(),
                cardCount = 0, successCount = 0, masteredCount = 0,
                isReported = false))
        )

        useCase(today)

        coVerify(exactly = 0) { sessionRepository.insertSession(any()) }
    }
}
