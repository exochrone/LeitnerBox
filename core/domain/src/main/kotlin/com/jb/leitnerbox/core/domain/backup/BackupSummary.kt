package com.jb.leitnerbox.core.domain.backup

data class BackupSummary(
    val exportDate: String,
    val deckCount: Int,
    val cardCount: Int,
    val sessionCount: Int
)
