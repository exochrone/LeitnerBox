package com.jb.leitnerbox.feature.stats.ui

import com.jb.leitnerbox.core.domain.usecase.stats.DeckStat
import com.jb.leitnerbox.core.domain.usecase.stats.GlobalSummary
import com.jb.leitnerbox.core.domain.usecase.stats.SessionHistoryItem

data class StatsUiState(
    val isLoading: Boolean = true,
    val globalSummary: GlobalSummary? = null,
    val deckStats: List<DeckStat> = emptyList(),
    val sessionHistory: List<SessionHistoryItem> = emptyList()
)

val StatsUiState.isEmpty: Boolean
    get() = !isLoading && globalSummary == null && deckStats.isEmpty()

val StatsUiState.hasNoData: Boolean
    get() = !isLoading && deckStats.isEmpty() && (globalSummary?.totalCards ?: 0) == 0

val StatsUiState.hasNoHistory: Boolean
    get() = !isLoading && sessionHistory.isEmpty()
