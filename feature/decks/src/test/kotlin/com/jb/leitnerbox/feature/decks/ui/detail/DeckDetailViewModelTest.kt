package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.*
import com.jb.leitnerbox.core.domain.usecase.tampon.ActivateDailyCardsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private val getDeckSummary = mockk<GetDeckSummaryUseCase>()
    private val deleteDeckUseCase = mockk<DeleteDeckUseCase>()
    private val updateDeckUseCase = mockk<UpdateDeckUseCase>()
    private val updateDeckColorUseCase = mockk<UpdateDeckColorUseCase>()
    private val activateDailyCardsUseCase = mockk<ActivateDailyCardsUseCase>()
    private val settingsRepository = mockk<SettingsRepository>()
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
    fun `uiState should emit the correct state`() = runTest {
        // Given
        val deckId = 1L
        val deck = Deck(id = deckId, name = "Test Deck")
        val summary = DeckSummary(0, emptyMap(), null, 0f, 5)
        
        every { getDeckByIdUseCase(deckId) } returns flowOf(deck)
        every { getCardsUseCase(deckId) } returns flowOf(emptyList())
        every { getDeckSummary(deckId, any()) } returns flowOf(summary)
        every { settingsRepository.getNewCardsPerDay() } returns flowOf(25)
        
        val savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = DeckDetailViewModel(
            getDeckByIdUseCase, 
            getCardsUseCase, 
            getDeckSummary,
            deleteDeckUseCase, 
            updateDeckUseCase,
            updateDeckColorUseCase,
            activateDailyCardsUseCase,
            settingsRepository,
            savedStateHandle
        )

        // Then
        val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(deck, viewModel.uiState.value.deck)
        assertEquals(5, viewModel.uiState.value.inactiveCardCount)
        collectJob.cancel()
    }
}
