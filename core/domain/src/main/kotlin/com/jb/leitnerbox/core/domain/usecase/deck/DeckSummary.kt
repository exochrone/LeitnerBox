package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Card
import java.time.Instant

data class DeckSummary(
    val cards: List<Card>,
    val masteredCount: Int,
    val cardsPerBox: Map<Int, Int>,
    val nextReviewDate: Instant?,
    val progress: Float
)
