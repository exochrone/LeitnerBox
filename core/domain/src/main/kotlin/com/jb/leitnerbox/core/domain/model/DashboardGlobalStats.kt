package com.jb.leitnerbox.core.domain.model

data class DashboardGlobalStats(
    val streak: Int,
    val totalCards: Int,
    val activeCards: Int,
    val masteredCards: Int,
    val successRate: Int?,          // null si aucune session
    val progressionGlobal: Int,     // progression globale toutes cartes
    val sessionCount: Int,
    val deckCount: Int
) {
    val masteredPercent: Int
        get() = if (totalCards == 0) 0 else (masteredCards * 100) / totalCards
}
