package com.jb.leitnerbox.feature.dashboard.ui

import com.jb.leitnerbox.core.domain.model.DashboardGlobalStats
import com.jb.leitnerbox.core.domain.model.SessionPlan
import com.jb.leitnerbox.core.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import com.jb.leitnerbox.core.domain.usecase.tampon.ActivateDailyCardsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getDailySessionPlan = mockk<GetDailySessionPlanUseCase>()
    private val getDashboardStats = mockk<GetDashboardStatsUseCase>()
    private val activateDailyCards = mockk<ActivateDailyCardsUseCase>()
    
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { getDailySessionPlan() } returns flowOf(SessionPlan(Instant.now(), emptyList()))
        every { getDashboardStats() } returns flowOf(DashboardGlobalStats(0, 0, 0, 0, 0, 0, 0, 0))
        
        viewModel = DashboardViewModel(
            getDailySessionPlan,
            getDashboardStats,
            activateDailyCards
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onResume declenche ActivateDailyCardsUseCase`() = runTest {
        coEvery { activateDailyCards() } returns Unit
        
        viewModel.onResume()
        advanceUntilIdle()
        
        coVerify(exactly = 1) { activateDailyCards() }
    }

    @Test
    fun `onResume appele plusieurs fois ne cause pas d erreur`() = runTest {
        coEvery { activateDailyCards() } returns Unit
        
        viewModel.onResume()
        viewModel.onResume()
        advanceUntilIdle()
        
        coVerify(exactly = 2) { activateDailyCards() }
    }
}
