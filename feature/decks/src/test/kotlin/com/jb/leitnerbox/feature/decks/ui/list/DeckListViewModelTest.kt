package com.jb.leitnerbox.feature.decks.ui.list

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository
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
    private lateinit var repository: DeckRepository
    private lateinit var viewModel: DeckListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `P3-UT-01 - decks should be empty when repository is empty`() {
        // Given
        every { repository.getDecksWithCardCount() } returns flowOf(emptyList())

        // When
        viewModel = DeckListViewModel(repository)

        // Then
        assertEquals(emptyList<Deck>(), viewModel.decks.value)
    }

    @Test
    fun `decks should emit list from repository`() {
        // Given
        val mockDecks = listOf(
            Deck(id = 1, name = "Deck 1"),
            Deck(id = 2, name = "Deck 2")
        )
        every { repository.getDecksWithCardCount() } returns flowOf(mockDecks)

        // When
        viewModel = DeckListViewModel(repository)

        // Then
        assertEquals(mockDecks, viewModel.decks.value)
    }
}
