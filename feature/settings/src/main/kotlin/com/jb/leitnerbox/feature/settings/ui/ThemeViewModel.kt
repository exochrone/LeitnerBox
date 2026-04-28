package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.usecase.settings.GetThemeUseCase
import com.jb.leitnerbox.core.domain.usecase.settings.SetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val getTheme: GetThemeUseCase,
    private val setTheme: SetThemeUseCase
) : ViewModel() {

    val currentTheme: StateFlow<AppTheme> = getTheme()
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM
        )

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch { setTheme(theme) }
    }
}
