package com.jb.leitnerbox.feature.session.selection

import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.SessionPlan
import com.jb.leitnerbox.core.domain.model.SessionPlanItem
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
    fun `initial state is loading`() {
        every { getDailySessionPlan(match { true }) } returns flowOf(SessionPlan(Instant.MIN, emptyList()))
        viewModel = SessionSelectionViewModel(getDailySessionPlan)
        
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads items from use case`() {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        every { getDailySessionPlan(match { true }) } returns flowOf(SessionPlan(Instant.MIN, items))
        
        viewModel = SessionSelectionViewModel(getDailySessionPlan)
        
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(5, viewModel.uiState.value.totalSelectedCards)
    }

    @Test
    fun `toggling item updates selection and total count`() {
        val deck = Deck(id = 1, name = "Test Deck")
        val items = listOf(SessionPlanItem(deck, 1, 5))
        every { getDailySessionPlan(match { true }) } returns flowOf(SessionPlan(Instant.MIN, items))
        
        viewModel = SessionSelectionViewModel(getDailySessionPlan)
        val item = viewModel.uiState.value.items[0]
        
        assertTrue(item.isSelected)
        
        viewModel.onBoxToggled(item)
        
        assertFalse(viewModel.uiState.value.items[0].isSelected)
        assertEquals(0, viewModel.uiState.value.totalSelectedCards)
        assertFalse(viewModel.uiState.value.canStart)
    }
}
