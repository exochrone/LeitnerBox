package com.jb.leitnerbox.core.domain.usecase.stats

data class GlobalSummary(
    val totalCards: Int,
    val masteredCards: Int,
    val inProgressCards: Int,
    val totalDecks: Int,
    val totalSessions: Int,
    val globalSuccessRate: Int?
)
