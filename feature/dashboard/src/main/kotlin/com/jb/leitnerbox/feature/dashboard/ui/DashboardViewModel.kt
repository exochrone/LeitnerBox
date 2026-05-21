package com.jb.leitnerbox.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.usecase.dashboard.GetDashboardStatsUseCase
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import com.jb.leitnerbox.core.domain.usecase.tampon.ActivateDailyCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDailySessionPlan: GetDailySessionPlanUseCase,
    private val getDashboardStats: GetDashboardStatsUseCase,
    private val activateDailyCards: ActivateDailyCardsUseCase
) : ViewModel() {

    private val _challengeDialog = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        getDailySessionPlan(),
        getDashboardStats(),
        _challengeDialog
    ) { plan, stats, showDialog ->
        DashboardUiState(
            sessionPlan             = plan,
            stats                   = stats,
            totalDecksCount         = stats.deckCount,
            masteredCardCount       = stats.masteredCards,
            isLoading               = false,
            showChallengeWarningDialog = showDialog
        )
    }.stateIn(
        scope   = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            activateDailyCards()
        }
    }

    fun onChallengeCardClicked() {
        if (uiState.value.masteredCardCount < 2) {
            _challengeDialog.value = true
        }
    }

    fun dismissChallengeDialog() {
        _challengeDialog.value = false
    }
}
