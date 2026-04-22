package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PostponeBoxSessionUseCaseTest {

    private val deckRepository = mockk<DeckRepository>()
    private val cardRepository = mockk<CardRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val nextSessionDateCalculator = mockk<NextSessionDateCalculator>()
    private val useCase = PostponeBoxSessionUseCase(deckRepository, cardRepository, settingsRepository, nextSessionDateCalculator)

    @Test
    fun `invoke postpones only cards due in the specified box`() = runTest {
        // Given
        val deckId = 1L
        val boxNumber = 1
        val excludedDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val deck = Deck(id = deckId, name = "Test Deck")
        
        val now = Instant.now()
        val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
        val tomorrow = today.plusDays(1)
        val tomorrowInstant = tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant()

        val cards = listOf(
            Card(id = 1, deckId = deckId, box = 1, nextReviewDate = now, recto = "R1", verso = "V1"), // Due, Box 1 -> Postpone
            Card(id = 2, deckId = deckId, box = 1, nextReviewDate = null, recto = "R2", verso = "V2"), // New, Box 1 -> Postpone
            Card(id = 3, deckId = deckId, box = 2, nextReviewDate = now, recto = "R3", verso = "V3"), // Due, Box 2 -> Skip
            Card(id = 4, deckId = deckId, box = 1, nextReviewDate = now.plusSeconds(3600), recto = "R4", verso = "V4") // Not due yet -> Skip
        )

        every { deckRepository.getDeckById(deckId) } returns flowOf(deck)
        every { cardRepository.getCardsByDeckId(deckId) } returns flowOf(cards)
        every { settingsRepository.getExcludedDays() } returns flowOf(excludedDays)
        every { nextSessionDateCalculator.calculateNextReviewDate(today, 1, excludedDays) } returns tomorrow
        coEvery { cardRepository.updateCard(any()) } returns Unit

        // When
        useCase(deckId, boxNumber)

        // Then
        coVerify(exactly = 1) { cardRepository.updateCard(match { it.id == 1L && it.nextReviewDate == tomorrowInstant }) }
        coVerify(exactly = 1) { cardRepository.updateCard(match { it.id == 2L && it.nextReviewDate == tomorrowInstant }) }
        coVerify(exactly = 2) { cardRepository.updateCard(any()) }
    }
}
