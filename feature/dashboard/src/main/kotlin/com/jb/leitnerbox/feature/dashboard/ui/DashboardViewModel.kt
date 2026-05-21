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

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        triggerCardActivation()

        combine(
            getDailySessionPlan(),
            getDashboardStats()
        ) { plan, stats ->
            _uiState.update {
                it.copy(
                    sessionPlan       = plan,
                    stats             = stats,
                    totalDecksCount   = stats.deckCount,
                    masteredCardCount = stats.masteredCards,
                    isLoading         = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun refreshDashboard() {
        triggerCardActivation()
    }

    private fun triggerCardActivation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            activateDailyCards()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onChallengeCardClicked() {
        if (_uiState.value.masteredCardCount < 2) {
            _uiState.update { it.copy(showChallengeWarningDialog = true) }
        }
    }

    fun dismissChallengeDialog() {
        _uiState.update { it.copy(showChallengeWarningDialog = false) }
    }
}
