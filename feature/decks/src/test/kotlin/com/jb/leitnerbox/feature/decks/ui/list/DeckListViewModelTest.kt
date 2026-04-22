package com.jb.leitnerbox.feature.decks.ui.list

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.DeckWithCardCount
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
import com.jb.leitnerbox.feature.decks.ui.list.model.DeckDisplayItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getDecksUseCase = mockk<GetDecksUseCase>()
    private val deleteDeckUseCase = mockk<DeleteDeckUseCase>()
    private val addDeckUseCase = mockk<AddDeckUseCase>()
    private lateinit var viewModel: DeckListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `P3-UT-01 - decks should be empty when repository is empty`() {
        // Given
        every { getDecksUseCase.executeWithCardCount() } returns flowOf(emptyList())

        // When
        viewModel = DeckListViewModel(getDecksUseCase, deleteDeckUseCase, addDeckUseCase)

        // Then
        assertEquals(emptyList<DeckDisplayItem>(), viewModel.decks.value)
    }

    @Test
    fun `decks should emit list from repository`() {
        // Given
        val mockDecks = listOf(
            DeckWithCardCount(Deck(id = 1, name = "Deck 1"), 0),
            DeckWithCardCount(Deck(id = 2, name = "Deck 2"), 0)
        )
        every { getDecksUseCase.executeWithCardCount() } returns flowOf(mockDecks)

        // When
        viewModel = DeckListViewModel(getDecksUseCase, deleteDeckUseCase, addDeckUseCase)

        // Then
        val expected = mockDecks.map { DeckDisplayItem(it.deck, it.cardCount) }
        assertEquals(expected, viewModel.decks.value)
    }
}
