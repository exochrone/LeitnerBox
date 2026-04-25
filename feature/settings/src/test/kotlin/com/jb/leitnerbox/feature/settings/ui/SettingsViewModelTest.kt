package com.jb.leitnerbox.feature.settings.ui

import android.content.Context
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.usecase.settings.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val getExcludedDays = mockk<GetExcludedDaysUseCase>()
    private val setExcludedDays = mockk<SetExcludedDaysUseCase>(relaxed = true)
    private val getTheme = mockk<GetThemeUseCase>()
    private val setTheme = mockk<SetThemeUseCase>(relaxed = true)
    private val getNotificationTime = mockk<GetNotificationTimeUseCase>()
    private val setNotificationTime = mockk<SetNotificationTimeUseCase>(relaxed = true)
    private val context = mockk<Context>()

    private val excludedDaysFlow = MutableStateFlow<Set<DayOfWeek>>(emptySet())
    private val themeFlow = MutableStateFlow(AppTheme.SYSTEM)
    private val notificationTimeFlow = MutableStateFlow(LocalTime.of(20, 0))

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getExcludedDays() } returns excludedDaysFlow
        every { getTheme() } returns themeFlow
        every { getNotificationTime() } returns notificationTimeFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel() {
        viewModel = SettingsViewModel(
            getExcludedDays, setExcludedDays,
            getTheme, setTheme,
            getNotificationTime, setNotificationTime,
            context
        )
    }

    @Test
    fun `P5-UT-30 Etat initial`() {
        initViewModel()
        val state = viewModel.uiState.value
        assertEquals(emptySet<DayOfWeek>(), state.excludedDays)
        assertEquals(AppTheme.SYSTEM, state.theme)
        assertEquals(LocalTime.of(20, 0), state.notificationTime)
    }

    @Test
    fun `P5-UT-31 Toggler samedi (non exclu - exclu)`() = runTest(testDispatcher) {
        initViewModel()
        viewModel.onDayToggled(DayOfWeek.SATURDAY)
        
        coVerify { setExcludedDays(setOf(DayOfWeek.SATURDAY)) }
    }

    @Test
    fun `P5-UT-32 Toggler samedi deux fois`() = runTest(testDispatcher) {
        excludedDaysFlow.value = setOf(DayOfWeek.SATURDAY)
        initViewModel()
        
        val job = launch { viewModel.uiState.collect() }

        viewModel.onDayToggled(DayOfWeek.SATURDAY)

        coVerify { setExcludedDays(emptySet()) }
        job.cancel()
    }

    @Test
    fun `P5-UT-33 Changer le theme en DARK`() = runTest(testDispatcher) {
        initViewModel()
        viewModel.onThemeSelected(AppTheme.DARK)

        coVerify { setTheme(AppTheme.DARK) }
    }

    @Test
    fun `P5-UT-34 Changer l'heure a 08-30`() = runTest(testDispatcher) {
        initViewModel()
        viewModel.onNotificationTimeSelected(8, 30)

        coVerify { setNotificationTime(LocalTime.of(8, 30)) }
    }
}
