package com.jb.leitnerbox.feature.decks.ui.list

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.DeckWithCardCount
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.card.InsertCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckSummaryUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.RestoreDeckUseCase
import com.jb.leitnerbox.feature.decks.ui.list.model.DeckDisplayItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DeckListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getDecksUseCase = mockk<GetDecksUseCase>()
    private val getDeckSummary = mockk<GetDeckSummaryUseCase>()
    private val deleteDeckUseCase = mockk<DeleteDeckUseCase>()
    private val addDeckUseCase = mockk<AddDeckUseCase>()
    private val restoreDeckUseCase = mockk<RestoreDeckUseCase>()
    private val getCardsUseCase = mockk<GetCardsUseCase>()
    private val insertCardsUseCase = mockk<InsertCardsUseCase>()
    private lateinit var viewModel: DeckListViewModel

    private val deckId = 1L
    private val deck = Deck(id = deckId, name = "Test Deck")
    private val deck2 = Deck(id = 2L, name = "Test Deck 2")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getDecksUseCase.executeWithCardCount() } returns flowOf(emptyList())
        viewModel = DeckListViewModel(
            getDecksUseCase,
            getDeckSummary,
            deleteDeckUseCase,
            addDeckUseCase,
            restoreDeckUseCase,
            getCardsUseCase,
            insertCardsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `P3-UT-01 - decks should be empty when repository is empty`() {
        // Given
        every { getDecksUseCase.executeWithCardCount() } returns flowOf(emptyList())

        // When (ViewModel already initialized in setup, but we re-init for specific behavior)
        viewModel = DeckListViewModel(
            getDecksUseCase,
            getDeckSummary,
            deleteDeckUseCase,
            addDeckUseCase,
            restoreDeckUseCase,
            getCardsUseCase,
            insertCardsUseCase
        )

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
        every { getDeckSummary(any(), any()) } returns flowOf(mockk(relaxed = true))

        // When
        viewModel = DeckListViewModel(
            getDecksUseCase,
            getDeckSummary,
            deleteDeckUseCase,
            addDeckUseCase,
            restoreDeckUseCase,
            getCardsUseCase,
            insertCardsUseCase
        )

        // Then
        assertEquals(2, viewModel.decks.value.size)
        assertEquals("Deck 1", viewModel.decks.value[0].deck.name)
        assertEquals("Deck 2", viewModel.decks.value[1].deck.name)
    }

    @Test
    fun `deleteDeck sauvegarde les cartes avant suppression`() = runTest {
        // Arrange
        val cards = listOf(createCard(1), createCard(2))
        coEvery { getCardsUseCase(deckId) } returns flowOf(cards)
        coEvery { deleteDeckUseCase(deck) } returns Unit

        // Act
        viewModel.deleteDeck(deck)

        // Assert
        coVerify { getCardsUseCase(deckId) }
        coVerify { deleteDeckUseCase(deck) }
    }

    @Test
    fun `undoDelete restaure le deck et ses cartes`() = runTest {
        // Arrange
        val cards = listOf(createCard(1), createCard(2))
        coEvery { getCardsUseCase(deckId) } returns flowOf(cards)
        coEvery { restoreDeckUseCase(deck) } returns Result.success(deckId)
        coEvery { insertCardsUseCase(cards) } returns Unit
        coEvery { deleteDeckUseCase(deck) } returns Unit

        viewModel.deleteDeck(deck)

        // Act
        viewModel.undoDelete(deck)

        // Assert
        coVerify { restoreDeckUseCase(deck) }
        coVerify { insertCardsUseCase(cards) }
    }

    @Test
    fun `undoDelete avec deck sans cartes ne plante pas`() = runTest {
        coEvery { getCardsUseCase(deckId) } returns flowOf(emptyList())
        coEvery { restoreDeckUseCase(deck) } returns Result.success(deckId)
        coEvery { insertCardsUseCase(emptyList()) } returns Unit
        coEvery { deleteDeckUseCase(deck) } returns Unit

        viewModel.deleteDeck(deck)
        viewModel.undoDelete(deck)

        coVerify { restoreDeckUseCase(deck) }
        coVerify { insertCardsUseCase(emptyList()) }
    }

    @Test
    fun `onDeleteConfirmed vide les cartes sauvegardees`() = runTest {
        val cards = listOf(createCard(1))
        coEvery { getCardsUseCase(deckId) } returns flowOf(cards)
        coEvery { restoreDeckUseCase(deck) } returns Result.success(deckId)
        coEvery { insertCardsUseCase(any()) } returns Unit
        coEvery { deleteDeckUseCase(deck) } returns Unit

        viewModel.deleteDeck(deck)
        viewModel.onDeleteConfirmed()

        // Une deuxième annulation ne devrait rien insérer (car deletedDeckCards est vidé)
        viewModel.undoDelete(deck)
        coVerify(exactly = 0) { insertCardsUseCase(cards) }
        coVerify { insertCardsUseCase(emptyList()) }
    }

    @Test
    fun `suppression successive écrase les cartes sauvegardées du deck précédent`() = runTest {
        val cards1 = listOf(createCard(1), createCard(2))
        val cards2 = listOf(createCard(3))
        coEvery { getCardsUseCase(deck.id) } returns flowOf(cards1)
        coEvery { getCardsUseCase(deck2.id) } returns flowOf(cards2)
        coEvery { deleteDeckUseCase(any()) } returns Unit
        coEvery { addDeckUseCase(any()) } returns Unit
        coEvery { insertCardsUseCase(any()) } returns Unit

        viewModel.deleteDeck(deck)
        viewModel.deleteDeck(deck2)  // écrase cards1

        viewModel.undoDelete(deck2)
        coVerify { insertCardsUseCase(cards2) }
        coVerify(exactly = 0) { insertCardsUseCase(cards1) }
    }

    private fun createCard(id: Long) = Card(
        id = id,
        deckId = deckId,
        recto = "Recto $id",
        verso = "Verso $id",
        rectoNormalized = "recto $id",
        box = 1
    )
}
