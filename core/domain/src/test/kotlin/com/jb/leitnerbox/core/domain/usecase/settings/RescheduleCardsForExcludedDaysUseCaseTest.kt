package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RescheduleCardsForExcludedDaysUseCaseTest {

    private val cardRepository = mockk<CardRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>()
    private val useCase = RescheduleCardsForExcludedDaysUseCase(cardRepository, settingsRepository)

    @Test
    fun `carte future sur jour exclu est avancee au jour suivant`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val nextSunday = nextSaturday.plusDays(1)

        val card = createCard(nextSaturday)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        val expectedDate = nextSunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        coVerify { cardRepository.updateCard(card.copy(nextReviewDate = expectedDate)) }
    }

    @Test
    fun `carte future avec plusieurs jours exclus consecutifs`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val nextMonday = nextSaturday.plusDays(2)

        val card = createCard(nextSaturday)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        val expectedDate = nextMonday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        coVerify { cardRepository.updateCard(card.copy(nextReviewDate = expectedDate)) }
    }

    @Test
    fun `carte planifiee aujourd'hui n'est pas modifiee`() = runTest {
        val today = LocalDate.now(ZoneId.systemDefault())
        val card = createCard(today)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(today.dayOfWeek))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `carte maitrisee n'est pas modifiee`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday).copy(isLearned = true)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `tous les jours exclus - aucune modification`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(DayOfWeek.entries.toSet())
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    private fun createCard(date: LocalDate) = Card(
        id = 1L,
        deckId = 1L,
        recto = "Q",
        verso = "A",
        nextReviewDate = date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        isLearned = false
    )
}
