package com.jb.leitnerbox.core.domain.usecase.deck

import java.time.Instant

data class DeckSummary(
    val masteredCount: Int,
    val cardsPerBox: Map<Int, Int>,
    val nextReviewDate: Instant?,
    val progress: Float
)
