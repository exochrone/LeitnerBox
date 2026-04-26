package com.jb.leitnerbox.feature.decks.ui.list.model

import com.jb.leitnerbox.core.domain.model.Deck

data class DeckDisplayItem(
    val deck: Deck,
    val cardCount: Int,
    val progress: Float = 0f
)
