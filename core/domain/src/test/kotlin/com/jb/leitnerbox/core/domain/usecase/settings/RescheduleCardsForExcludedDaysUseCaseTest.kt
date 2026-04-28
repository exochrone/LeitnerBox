package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class RescheduleCardsForExcludedDaysUseCaseTest {

    private val cardRepository = mockk<CardRepository>(relaxed = true)
    private val deckRepository = mockk<DeckRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var useCase: RescheduleCardsForExcludedDaysUseCase

    private val defaultDeck = Deck(
        id = 1L,
        name = "Test Deck",
        intervals = listOf(1, 3, 5, 7, 14)
    )

    @BeforeEach
    fun setup() {
        useCase = RescheduleCardsForExcludedDaysUseCase(cardRepository, deckRepository, settingsRepository)
        every { deckRepository.getDecks() } returns flowOf(listOf(defaultDeck))
    }

    @Test
    fun `carte future sur jour exclu est avancee au jour suivant`() = runTest {
        val today = LocalDate.now(ZoneId.systemDefault())
        val nextSaturday = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val nextSunday = nextSaturday.plusDays(1)

        // On part d'une carte révisée le vendredi (box 1, intervalle 1j) -> prévue samedi
        val lastReview = nextSaturday.minusDays(1)
        val card = createCard(nextSaturday, lastReview = lastReview)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        val expectedDate = nextSunday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        coVerify { cardRepository.updateCard(card.copy(nextReviewDate = expectedDate)) }
    }

    @Test
    fun `recalcul depuis lastReviewDate même si nextReviewDate était déjà décalée`() = runTest {
        val today = LocalDate.now(ZoneId.systemDefault())
        val nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        val previousFriday = nextMonday.minusDays(3)
        
        // naturalDate = Vendredi + 1j = Samedi
        // Mais on décale Samedi et Dimanche.
        // On suppose que la carte avait une nextReviewDate fantaisiste (ex: Mardi)
        val card = createCard(nextMonday.plusDays(1), lastReview = previousFriday)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        // Résultat attendu : Lundi (car Sam/Dim exclus)
        val expectedDate = nextMonday.atStartOfDay(ZoneId.systemDefault()).toInstant()
        coVerify { cardRepository.updateCard(card.copy(nextReviewDate = expectedDate)) }
    }

    @Test
    fun `carte planifiee aujourd'hui n'est pas modifiee`() = runTest {
        val today = LocalDate.now(ZoneId.systemDefault())
        val yesterday = today.minusDays(1)
        val card = createCard(today, lastReview = yesterday)
        
        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(today.dayOfWeek))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `tous les jours exclus - aucune modification`() = runTest {
        val today = LocalDate.now(ZoneId.systemDefault())
        val nextSaturday = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday, lastReview = nextSaturday.minusDays(1))
        
        every { settingsRepository.getExcludedDays() } returns flowOf(DayOfWeek.entries.toSet())
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `carte maitrisee n'est pas modifiee`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday, lastReview = nextSaturday.minusDays(1))
            .copy(isLearned = true)

        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `carte sans lastReviewDate n'est pas modifiee`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday, lastReview = nextSaturday.minusDays(1))
            .copy(lastReviewDate = null)

        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `carte dont la date naturelle n'est pas un jour exclu n'est pas modifiee`() = runTest {
        val nextMonday = LocalDate.now(ZoneId.systemDefault())
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        // Révisée dimanche, intervalle 1j → naturellement lundi (non exclu)
        val card = createCard(nextMonday, lastReview = nextMonday.minusDays(1))

        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `carte dont le deck est introuvable est ignoree`() = runTest {
        val nextSaturday = LocalDate.now(ZoneId.systemDefault())
            .with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        val card = createCard(nextSaturday, lastReview = nextSaturday.minusDays(1))
            .copy(deckId = 999L)  // deck inexistant

        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()  // ne doit pas crasher

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    @Test
    fun `pas d'ecriture en base si la date est deja correcte`() = runTest {
        val nextSunday = LocalDate.now(ZoneId.systemDefault())
            .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        // Révisée samedi, intervalle 1j → naturellement dimanche
        // Samedi exclu → adjusted = dimanche = currentDate → pas d'update
        val card = createCard(nextSunday, lastReview = nextSunday.minusDays(1))

        every { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY))
        every { cardRepository.getAllCards() } returns flowOf(listOf(card))

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCard(any()) }
    }

    private fun createCard(nextReview: LocalDate, lastReview: LocalDate) = Card(
        id = 1L,
        deckId = 1L,
        recto = "Q",
        verso = "A",
        lastReviewDate = lastReview.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        nextReviewDate = nextReview.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        isLearned = false,
        box = 1
    )
}
