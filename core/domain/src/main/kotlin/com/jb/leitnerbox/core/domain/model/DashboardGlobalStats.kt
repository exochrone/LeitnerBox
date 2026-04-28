package com.jb.leitnerbox.core.domain.model

data class DashboardGlobalStats(
    val streak: Int,
    val totalCards: Int,
    val masteredCards: Int,
    val successRate: Int?,          // null si aucune session
    val progressionByDeck: Int,     // Option A : moyenne des progressions par deck
    val progressionGlobal: Int,     // Option B : progression globale toutes cartes
    val sessionCount: Int,
    val deckCount: Int
) {
    val masteredPercent: Int
        get() = if (totalCards == 0) 0 else (masteredCards * 100) / totalCards
}
