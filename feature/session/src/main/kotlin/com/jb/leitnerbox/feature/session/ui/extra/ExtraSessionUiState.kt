package com.jb.leitnerbox.feature.session.ui.extra

import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card

data class ExtraSessionUiState(
    val cards: List<Card> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val evaluatedCount: Int = 0,
    val successCount: Int = 0,
    val userInput: String = "",
    val inputValidated: Boolean = false,
    val checkResult: AnswerCheckResult? = null,
    val currentDeckName: String = "",
    val isLoading: Boolean = true
)

val ExtraSessionUiState.progressFraction: Float
    get() = if (cards.isEmpty()) 0f
            else evaluatedCount.toFloat() / cards.size

val ExtraSessionUiState.currentCard: Card?
    get() = cards.getOrNull(currentIndex)
