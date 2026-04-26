package com.jb.leitnerbox.feature.decks.ui.list.model

import com.jb.leitnerbox.core.domain.model.Deck
import java.time.Instant

data class DeckDisplayItem(
    val deck: Deck,
    val totalCardCount: Int,
    val masteredCount: Int = 0,
    val cardsPerBox: Map<Int, Int> = emptyMap(),
    val nextReviewDate: Instant? = null,
    val progress: Float = 0f
)
