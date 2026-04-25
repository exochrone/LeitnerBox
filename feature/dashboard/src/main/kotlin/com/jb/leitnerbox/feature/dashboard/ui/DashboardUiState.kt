package com.jb.leitnerbox.feature.dashboard.ui

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val totalCardsToReview: Int,
        val decksWithReviews: Int,
        val streak: Int
    ) : DashboardUiState
    data class Empty(
        val streak: Int
    ) : DashboardUiState
}
