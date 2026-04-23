package com.jb.leitnerbox.feature.dashboard.ui

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val totalCardsToReview: Int,
        val decksWithReviews: Int
    ) : DashboardUiState
    object Empty : DashboardUiState
}