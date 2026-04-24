package com.jb.leitnerbox.feature.session.ui

import com.jb.leitnerbox.core.domain.model.Card

data class SessionUiState(
    val currentCard: Card? = null,
    val isFlipped: Boolean = false,
    val currentIndex: Int = 0,
    val totalCards: Int = 0,
    val successCount: Int = 0,
    val evaluatedCount: Int = 0
) {
    val progress: Float
        get() = if (totalCards > 0) (currentIndex + 1).toFloat() / totalCards else 0f
}
