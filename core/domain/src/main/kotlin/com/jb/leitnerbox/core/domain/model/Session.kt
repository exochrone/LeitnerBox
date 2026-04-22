package com.jb.leitnerbox.core.domain.model

import java.time.Instant

data class Session(
    val id: Long = 0,
    val date: Instant,
    val deckIds: List<Long>,
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int,
    val isReported: Boolean = false
)