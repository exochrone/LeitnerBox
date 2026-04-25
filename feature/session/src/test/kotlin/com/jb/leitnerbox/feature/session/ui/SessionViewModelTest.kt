package com.jb.leitnerbox.feature.session.ui

import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
import com.jb.leitnerbox.core.domain.usecase.card.EvaluateCardUseCase
import com.jb.leitnerbox.core.domain.usecase.session.SaveSessionUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val sessionStateHolder = SessionStateHolder()
    private val evaluateCard = mockk<EvaluateCardUseCase>(relaxed = true)
    private val checkAnswer = mockk<CheckAnswerUseCase>()
    private val saveSession = mockk<SaveSessionUseCase>(relaxed = true)
    private lateinit var viewModel: SessionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel(cards: List<Card> = emptyList(), selectedItems: List<SessionPlanItem> = emptyList()) {
        sessionStateHolder.pendingCards = cards
        sessionStateHolder.selectedItems = selectedItems
        viewModel = SessionViewModel(sessionStateHolder, evaluateCard, checkAnswer, saveSession)
        testDispatcher.scheduler.runCurrent()
    }

    @Test
    fun `P4-UT-01 Initialiser une session avec cartes`() {
        val cards = listOf(Card(id = 1, deckId = 1, recto = "Q1", verso = "A1"))
        initViewModel(cards)

        assertEquals(1, viewModel.uiState.value.cards.size)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        assertNotNull(viewModel.uiState.value.currentCard)
    }

    @Test
    fun `P4-UT-02 onFlipCard updates isFlipped`() {
        initViewModel(listOf(Card(id = 1, deckId = 1, recto = "Q1", verso = "A1")))
        
        assertFalse(viewModel.uiState.value.isFlipped)
        viewModel.onFlipCard()
        assertTrue(viewModel.uiState.value.isFlipped)
    }

    @Test
    fun `P4-UT-03 onFlipCard twice returns to false`() {
        initViewModel(listOf(Card(id = 1, deckId = 1, recto = "Q1", verso = "A1")))
        
        viewModel.onFlipCard()
        assertTrue(viewModel.uiState.value.isFlipped)
        viewModel.onFlipCard()
        assertFalse(viewModel.uiState.value.isFlipped)
    }

    @Test
    fun `P4-UT-04 onEvaluate good answer increments index and calls use case`() = runTest {
        val deck = Deck(id = 1, name = "Deck", intervals = listOf(1, 2, 3))
        val cards = listOf(
            Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 1),
            Card(id = 2, deckId = 1, recto = "Q2", verso = "A2", box = 1)
        )
        val selectedItems = listOf(SessionPlanItem(deck, 1, 2))
        initViewModel(cards, selectedItems)

        viewModel.onEvaluate(true)
        testDispatcher.scheduler.runCurrent()

        coVerify { evaluateCard(cards[0], deck, true, any()) }
        assertEquals(1, viewModel.uiState.value.currentIndex)
        assertEquals(1, viewModel.uiState.value.successCount)
        assertEquals(1, viewModel.uiState.value.advancedCount)
    }

    @Test
    fun `P4-UT-09 good answer on last box sends CardMastered event and sets transition`() = runTest {
        val deck = Deck(id = 1, name = "Deck", intervals = listOf(1))
        val card = Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 1)
        val selectedItems = listOf(SessionPlanItem(deck, 1, 1))
        initViewModel(listOf(card), selectedItems)

        val events = mutableListOf<SessionUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onEvaluate(true)
        testDispatcher.scheduler.runCurrent()

        assertTrue(events.contains(SessionUiEvent.CardMastered))
        assertEquals(1, viewModel.uiState.value.masteredThisSession)
        assertTrue(viewModel.uiState.value.isMasteredTransition)
        // Ne doit pas encore avoir bougé à l'index suivant (ici c'était la seule carte donc l'index reste 0)
        assertEquals(0, viewModel.uiState.value.currentIndex)
        
        // Simuler la fin de la célébration
        viewModel.onMasteryCelebrationFinished()
        testDispatcher.scheduler.advanceTimeBy(300)
        testDispatcher.scheduler.runCurrent()
        
        assertFalse(viewModel.uiState.value.isMasteredTransition)
        // Ici comme c'était la seule carte, on finit la session
        assertTrue(events.contains(SessionUiEvent.SessionFinished))

        job.cancel()
    }
    
    @Test
    fun `onEvaluate bad answer on box 1 does not increment retreatedCount`() = runTest {
        val deck = Deck(id = 1, name = "Deck", intervals = listOf(1, 2))
        val card = Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 1)
        val selectedItems = listOf(SessionPlanItem(deck, 1, 1))
        initViewModel(listOf(card), selectedItems)

        viewModel.onEvaluate(false)
        testDispatcher.scheduler.runCurrent()

        assertEquals(0, viewModel.uiState.value.retreatedCount)
    }

    @Test
    fun `onEvaluate bad answer on box 2 increments retreatedCount`() = runTest {
        val deck = Deck(id = 1, name = "Deck", intervals = listOf(1, 2))
        val card = Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 2)
        val selectedItems = listOf(SessionPlanItem(deck, 2, 1))
        initViewModel(listOf(card), selectedItems)

        viewModel.onEvaluate(false)
        testDispatcher.scheduler.runCurrent()

        assertEquals(1, viewModel.uiState.value.retreatedCount)
    }

    @Test
    fun `session end saves session and sends event`() = runTest {
        val deck = Deck(id = 1, name = "Deck", intervals = listOf(1))
        val card = Card(id = 1, deckId = 1, recto = "Q1", verso = "A1", box = 1, needsInput = false)
        initViewModel(listOf(card), listOf(SessionPlanItem(deck, 1, 1)))

        val events = mutableListOf<SessionUiEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onEvaluate(true)
        testDispatcher.scheduler.runCurrent()

        // Si la carte est maîtrisée, on doit appeler onMasteryCelebrationFinished pour finir
        if (viewModel.uiState.value.isMasteredTransition) {
            viewModel.onMasteryCelebrationFinished()
            testDispatcher.scheduler.advanceTimeBy(300)
            testDispatcher.scheduler.runCurrent()
        }

        coVerify { saveSession(any()) }
        assertTrue(events.contains(SessionUiEvent.SessionFinished))
        assertNotNull(sessionStateHolder.lastSessionResult)

        job.cancel()
    }
}
