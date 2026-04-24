package com.jb.leitnerbox.feature.session.ui

import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card

data class SessionUiState(
    val cards: List<Card> = emptyList(),
    val currentCard: Card? = null,
    val isFlipped: Boolean = false,
    val currentIndex: Int = 0,
    val successCount: Int = 0,
    val evaluatedCount: Int = 0,
    val advancedCount: Int = 0,
    val retreatedCount: Int = 0,
    val isChallenge: Boolean = false,
    val masteredThisSession: Int = 0,
    val userInput: String = "",
    val inputValidated: Boolean = false,
    val checkResult: AnswerCheckResult? = null
)

// Propriétés dérivées (calculées hors du composable)
val SessionUiState.progressCurrent: Int
    get() = if (isChallenge) masteredThisSession else evaluatedCount

val SessionUiState.progressTotal: Int
    get() = cards.size

val SessionUiState.progressFraction: Float
    get() = if (progressTotal == 0) 0f
    else progressCurrent.toFloat() / progressTotal
