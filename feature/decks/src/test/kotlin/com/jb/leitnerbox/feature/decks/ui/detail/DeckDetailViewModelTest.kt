package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var deckRepository: DeckRepository
    private lateinit var cardRepository: CardRepository
    private lateinit var viewModel: DeckDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        deckRepository = mockk()
        cardRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deck should emit the correct deck from repository`() = runTest {
        // Given
        val deckId = 1L
        val deck = Deck(id = deckId, name = "Test Deck")
        
        every { deckRepository.getDeckById(deckId) } returns flowOf(deck)
        every { cardRepository.getCardsByDeckId(deckId) } returns flowOf(emptyList())
        
        val savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        // Then
        advanceUntilIdle()
        assertEquals(deck, viewModel.deck.value)
    }
}
