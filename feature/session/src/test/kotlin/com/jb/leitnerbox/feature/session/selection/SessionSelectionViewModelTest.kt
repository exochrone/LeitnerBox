package com.jb.leitnerbox.feature.session.selection

import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.session.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionSelectionViewModelTest {

    private val getDailySessionPlan = mockk<GetDailySessionPlanUseCase>()
    private val buildSession = mockk<BuildSessionUseCase>()
    private val postponeBoxSession = mockk<PostponeBoxSessionUseCase>()
    private val cancelPostponeBox = mockk<CancelPostponeBoxUseCase>()
    private val saveSession = mockk<SaveSessionUseCase>()
    private val sessionStateHolder = SessionStateHolder()
    private lateinit var viewModel: SessionSelectionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `after plan loads, isLoading is false`() {
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, emptyList()))
        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads items from use case`() {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, items))
        
        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(5, viewModel.uiState.value.totalSelectedCards)
    }

    @Test
    fun `toggling item updates selection and total count`() {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, items))
        
        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        val item = viewModel.uiState.value.items[0]
        
        assertTrue(item.isSelected)
        
        viewModel.onBoxToggled(item)
        
        assertFalse(viewModel.uiState.value.items[0].isSelected)
        assertEquals(0, viewModel.uiState.value.totalSelectedCards)
        assertFalse(viewModel.uiState.value.canStart)
    }

    @Test
    fun `onStartSession updates state holder and emits event`() = runTest {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        val cards = listOf(Card(id = 1, deckId = 1, recto = "Q", verso = "A"))
        
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, items))
        coEvery { buildSession(any()) } returns cards
        
        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        
        val events = mutableListOf<SessionSelectionEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        
        // Initial plan load collection
        runCurrent()

        viewModel.onStartSession()
        
        // Use testScheduler for advanceUntilIdle
        testScheduler.advanceUntilIdle()
        
        assertEquals(cards, sessionStateHolder.pendingCards)
        assertEquals(1, events.size)
        assertTrue(events[0] is SessionSelectionEvent.NavigateToSession)
        
        job.cancel()
    }

    @Test
    fun `onPostponeBox calls use cases and emits event`() = runTest {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, items))
        coEvery { postponeBoxSession(any(), any()) } returns Unit
        coEvery { saveSession(any()) } returns 123L

        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        
        val events = mutableListOf<SessionSelectionEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }
        runCurrent()

        val item = viewModel.uiState.value.items[0]
        viewModel.onPostponeBox(item)
        testScheduler.advanceUntilIdle()

        coVerify { postponeBoxSession(deckId = 1L, boxNumber = 1) }
        coVerify { saveSession(match { it.isReported && it.deckIds.contains(1L) }) }
        
        val event = events.last() as SessionSelectionEvent.ShowUndoPostpone
        assertEquals(1L, event.deckId)
        assertEquals("Test Deck", event.deckName)
        assertEquals(1, event.boxNumber)
        assertEquals(123L, event.sessionId)
        
        job.cancel()
    }

    @Test
    fun `onUndoPostpone calls cancel use case`() = runTest {
        every { getDailySessionPlan(any()) } returns flowOf(SessionPlan(Instant.MIN, emptyList()))
        coEvery { cancelPostponeBox(any(), any(), any()) } returns Unit
        
        viewModel = SessionSelectionViewModel(
            getDailySessionPlan,
            buildSession,
            postponeBoxSession,
            cancelPostponeBox,
            saveSession,
            sessionStateHolder
        )
        
        viewModel.onUndoPostpone(deckId = 1L, boxNumber = 1, sessionId = 123L)
        testScheduler.advanceUntilIdle()
        
        coVerify { cancelPostponeBox(1L, 1, 123L) }
    }
}
