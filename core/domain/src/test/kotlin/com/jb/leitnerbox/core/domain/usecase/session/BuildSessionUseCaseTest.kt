package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.SessionPlanItem
import com.jb.leitnerbox.core.domain.repository.CardRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildSessionUseCaseTest {

    private val cardRepository = mockk<CardRepository>()
    private val useCase = BuildSessionUseCase(cardRepository)

    @Test
    fun `should return shuffled cards from selected items`() = runBlocking {
        val deck1 = Deck(id = 1, name = "Deck 1")
        val deck2 = Deck(id = 2, name = "Deck 2")
        
        val cards1 = listOf(
            Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 1),
            Card(id = 2, deckId = 1, recto = "Q2", verso = "A2", box = 2)
        )
        val cards2 = listOf(
            Card(id = 3, deckId = 2, recto = "Q3", verso = "A3", box = 1),
            Card(id = 4, deckId = 2, recto = "Q4", verso = "A4", box = 1, isLearned = true)
        )

        every { cardRepository.getCardsByDeckId(1) } returns flowOf(cards1)
        every { cardRepository.getCardsByDeckId(2) } returns flowOf(cards2)

        val selectedItems = listOf(
            SessionPlanItem(deck1, 1, 1),
            SessionPlanItem(deck2, 1, 1)
        )

        val result = useCase(selectedItems)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == 1L })
        assertTrue(result.any { it.id == 3L })
    }
}
