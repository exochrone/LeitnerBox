package com.jb.leitnerbox.core.data.repository

import com.jb.leitnerbox.core.data.local.dao.DeckDao
import com.jb.leitnerbox.core.data.local.entity.DeckEntity
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeckRepositoryImplTest {

    private val dao = mockk<DeckDao>()
    private val repository = DeckRepositoryImpl(dao)

    @Test
    fun `getDecks returns domain models`() = runTest {
        // Given
        val entities = listOf(
            DeckEntity(id = 1, name = "Deck 1", description = "Desc 1", intervals = listOf(1), wrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE, presentationOrder = PresentationOrder.RANDOM)
        )
        every { dao.getDecks() } returns flowOf(entities)

        // When
        val result = repository.getDecks().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Deck 1", result[0].name)
    }

    @Test
    fun `insertDeck calls dao with entity`() = runTest {
        // Given
        val deck = Deck(name = "New Deck")
        coEvery { dao.insertDeck(any()) } returns 1L

        // When
        val id = repository.insertDeck(deck)

        // Then
        assertEquals(1L, id)
        coVerify { dao.insertDeck(match { it.name == "New Deck" }) }
    }
}
