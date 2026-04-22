package com.jb.leitnerbox.feature.decks.ui.edit

import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var addDeckUseCase: AddDeckUseCase
    private lateinit var viewModel: DeckEditViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addDeckUseCase = mockk()
        viewModel = DeckEditViewModel(addDeckUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `P3-UT-02 - saveDeck should call usecase when name is valid`() {
        // Given
        val deckName = "New Deck"
        viewModel.onNameChange(deckName)
        coEvery { addDeckUseCase(any()) } returns Result.success(1L)

        // When
        viewModel.saveDeck()

        // Then
        coVerify { addDeckUseCase(match { it.name == deckName && it.intervals.size == 5 }) }
    }

    @Test
    fun `P3-UT-03 - saveDeck should show error when name is empty`() {
        // Given
        viewModel.onNameChange("")

        // When
        viewModel.saveDeck()

        // Then
        assertTrue(viewModel.uiState.value.nameError)
        coVerify(exactly = 0) { addDeckUseCase(any()) }
    }
}
