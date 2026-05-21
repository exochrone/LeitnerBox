package com.jb.leitnerbox.feature.decks.ui.edit

import androidx.lifecycle.SavedStateHandle
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.UpdateDeckUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getDeckByIdUseCase = mockk<GetDeckByIdUseCase>(relaxed = true)
    private val addDeckUseCase = mockk<AddDeckUseCase>(relaxed = true)
    private val updateDeckUseCase = mockk<UpdateDeckUseCase>(relaxed = true)
    private lateinit var viewModel: DeckEditViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DeckEditViewModel(
            SavedStateHandle(),
            getDeckByIdUseCase,
            addDeckUseCase,
            updateDeckUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveDeck should call usecase when name is valid`() = runTest {
        // Given
        val deckName = "New Deck"
        viewModel.onNameChange(deckName)
        viewModel.nextStep() // Step 1 to 2
        
        // When
        viewModel.nextStep() // Step 2 triggers saveDeck

        // Then
        coVerify { addDeckUseCase(match { it.name == deckName && it.intervals.size == 5 }) }
    }

    @Test
    fun `nextStep should show error when name is empty`() = runTest {
        // Given
        viewModel.onNameChange("")

        // When
        viewModel.nextStep()

        // Then
        assertTrue(viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { addDeckUseCase(any()) }
    }
}
