package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.usecase.settings.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getExcludedDays: GetExcludedDaysUseCase,
    private val setExcludedDays: SetExcludedDaysUseCase,
    private val getTheme: GetThemeUseCase,
    private val setTheme: SetThemeUseCase,
    private val getNotificationTime: GetNotificationTimeUseCase,
    private val setNotificationTime: SetNotificationTimeUseCase,
    private val rescheduleNotification: RescheduleNotificationUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        getExcludedDays(),
        getTheme(),
        getNotificationTime()
    ) { excludedDays, theme, notifTime ->
        SettingsUiState(
            excludedDays = excludedDays,
            theme = theme,
            notificationTime = notifTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onDayToggled(day: DayOfWeek) {
        viewModelScope.launch {
            val current = uiState.value.excludedDays.toMutableSet()
            if (day in current) current.remove(day) else current.add(day)
            setExcludedDays(current)
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { setTheme(theme) }
    }

    fun onNotificationTimeSelected(hour: Int, minute: Int) {
        viewModelScope.launch {
            val newTime = LocalTime.of(hour, minute)
            setNotificationTime(newTime)
            rescheduleNotification(newTime)
        }
    }
}
