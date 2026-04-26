package com.jb.leitnerbox.feature.stats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.usecase.stats.GetDeckStatsUseCase
import com.jb.leitnerbox.core.domain.usecase.stats.GetGlobalSummaryUseCase
import com.jb.leitnerbox.core.domain.usecase.stats.GetSessionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getGlobalSummary: GetGlobalSummaryUseCase,
    private val getDeckStats: GetDeckStatsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        getGlobalSummary(),
        getDeckStats(),
        getSessionHistory()
    ) { summary, deckStats, history ->
        StatsUiState(
            isLoading = false,
            globalSummary = summary,
            deckStats = deckStats,
            sessionHistory = history
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(isLoading = true)
    )
}
