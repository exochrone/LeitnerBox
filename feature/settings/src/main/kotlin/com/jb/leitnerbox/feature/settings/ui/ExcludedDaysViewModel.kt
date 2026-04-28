package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.usecase.settings.GetExcludedDaysUseCase
import com.jb.leitnerbox.core.domain.usecase.settings.SetExcludedDaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class ExcludedDaysViewModel @Inject constructor(
    private val getExcludedDays: GetExcludedDaysUseCase,
    private val setExcludedDays: SetExcludedDaysUseCase
) : ViewModel() {

    val excludedDays: StateFlow<Set<DayOfWeek>> = getExcludedDays()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    fun onDayToggled(day: DayOfWeek) {
        viewModelScope.launch {
            val current = excludedDays.value.toMutableSet()
            if (day in current) current.remove(day) else current.add(day)
            setExcludedDays(current)
        }
    }
}
