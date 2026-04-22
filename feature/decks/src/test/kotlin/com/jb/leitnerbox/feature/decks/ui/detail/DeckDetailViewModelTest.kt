package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
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
    private val getDeckByIdUseCase = mockk<GetDeckByIdUseCase>()
    private val getCardsUseCase = mockk<GetCardsUseCase>()
    private val deleteDeckUseCase = mockk<DeleteDeckUseCase>()
    private val addDeckUseCase = mockk<AddDeckUseCase>()
    private lateinit var viewModel: DeckDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
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
        
        every { getDeckByIdUseCase(deckId) } returns flowOf(deck)
        every { getCardsUseCase(deckId) } returns flowOf(emptyList())
        
        val savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = DeckDetailViewModel(getDeckByIdUseCase, getCardsUseCase, deleteDeckUseCase, addDeckUseCase, savedStateHandle)

        // Then
        advanceUntilIdle()
        assertEquals(deck, viewModel.deck.value)
    }
}
