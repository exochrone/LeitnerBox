package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetDailySessionPlanUseCaseTest {

    private val deckRepository = mockk<DeckRepository>()
    private val cardRepository = mockk<CardRepository>()
    private val useCase = GetDailySessionPlanUseCase(deckRepository, cardRepository)

    @Test
    fun `invoke returns correct plan for multiple decks and boxes`() = runTest {
        // Given
        val now = Instant.now()
        val deck1 = Deck(id = 1, name = "Deck 1")
        val deck2 = Deck(id = 2, name = "Deck 2")
        
        val yesterday = now.minus(1, ChronoUnit.DAYS)
        val tomorrow = now.plus(1, ChronoUnit.DAYS)

        val cards1 = listOf(
            Card(id = 1, deckId = 1, box = 1, nextReviewDate = yesterday, recto = "R1", verso = "V1"),
            Card(id = 2, deckId = 1, box = 1, nextReviewDate = now, recto = "R2", verso = "V2"),
            Card(id = 3, deckId = 1, box = 2, nextReviewDate = tomorrow, recto = "R3", verso = "V3")
        )
        
        val cards2 = listOf(
            Card(id = 4, deckId = 2, box = 1, nextReviewDate = now, recto = "R4", verso = "V4")
        )

        every { deckRepository.getDecks() } returns flowOf(listOf(deck1, deck2))
        every { cardRepository.getCardsByDeckId(1) } returns flowOf(cards1)
        every { cardRepository.getCardsByDeckId(2) } returns flowOf(cards2)

        // When
        val plan = useCase(now).first()

        // Then
        assertEquals(2, plan.items.size)
        
        val deck1Box1Item = plan.items.find { it.deck.id == 1L && it.boxNumber == 1 }
        val deck2Box1Item = plan.items.find { it.deck.id == 2L && it.boxNumber == 1 }
        
        assertEquals(2, deck1Box1Item?.cardCount)
        assertEquals(1, deck2Box1Item?.cardCount)
        
        // Ensure box 2 from deck 1 is NOT included (it's due tomorrow)
        val deck1Box2Item = plan.items.find { it.deck.id == 1L && it.boxNumber == 2 }
        assertEquals(null, deck1Box2Item)
    }

    @Test
    fun `invoke excludes learned cards`() = runTest {
        // Given
        val now = Instant.now()
        val deck1 = Deck(id = 1, name = "Deck 1")
        val yesterday = now.minus(1, ChronoUnit.DAYS)

        val cards = listOf(
            Card(id = 1, deckId = 1, box = 1, nextReviewDate = yesterday, recto = "R1", verso = "V1", isLearned = true),
            Card(id = 2, deckId = 1, box = 1, nextReviewDate = now, recto = "R2", verso = "V2", isLearned = false)
        )

        every { deckRepository.getDecks() } returns flowOf(listOf(deck1))
        every { cardRepository.getCardsByDeckId(1) } returns flowOf(cards)

        // When
        val plan = useCase(now).first()

        // Then
        assertEquals(1, plan.items.size)
        assertEquals(1, plan.items.first().cardCount)
    }

    @Test
    fun `invoke returns empty plan when no decks exist`() = runTest {
        // Given
        every { deckRepository.getDecks() } returns flowOf(emptyList())

        // When
        val plan = useCase().first()

        // Then
        assertEquals(0, plan.items.size)
    }
}
