package com.jb.leitnerbox.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDailySessionPlanUseCase: GetDailySessionPlanUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getDailySessionPlanUseCase()
        .map { plan ->
            if (plan.items.isEmpty()) {
                DashboardUiState.Empty
            } else {
                DashboardUiState.Success(
                    totalCardsToReview = plan.items.sumOf { it.cardCount },
                    decksWithReviews = plan.items.map { it.deck.id }.distinct().size
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )
}