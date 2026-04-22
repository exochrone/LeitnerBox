package com.jb.leitnerbox.feature.cards.ui.edit

import androidx.lifecycle.SavedStateHandle
import com.jb.leitnerbox.core.domain.usecase.card.AddCardUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var addCardUseCase: AddCardUseCase
    private lateinit var viewModel: CardEditViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addCardUseCase = mockk()
        val savedStateHandle = SavedStateHandle(mapOf("deckId" to 1L))
        viewModel = CardEditViewModel(addCardUseCase, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `P3-UT-04 - saveCard should call usecase when data is valid`() {
        // Given
        viewModel.onRectoChange("Question")
        viewModel.onVersoChange("Réponse")
        coEvery { addCardUseCase(any()) } returns Result.success(1L)

        // When
        viewModel.saveCard()

        // Then
        coVerify { addCardUseCase(match { it.recto == "Question" && it.verso == "Réponse" && it.deckId == 1L }) }
    }

    @Test
    fun `P3-UT-05 - saveCard should show error when recto is empty`() {
        // Given
        viewModel.onRectoChange("")
        viewModel.onVersoChange("Réponse")

        // When
        viewModel.saveCard()

        // Then
        assertTrue(viewModel.uiState.value.rectoError)
        coVerify(exactly = 0) { addCardUseCase(any()) }
    }
}
