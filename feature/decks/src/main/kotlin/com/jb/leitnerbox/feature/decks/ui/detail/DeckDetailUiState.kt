package com.jb.leitnerbox.feature.decks.ui.detail

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck

data class DeckDetailUiState(
    val deck: Deck? = null,
    val cards: List<Card> = emptyList(),
    val progress: Float = 0f,
    val isLoading: Boolean = true
)
