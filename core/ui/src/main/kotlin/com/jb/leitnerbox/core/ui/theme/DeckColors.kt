package com.jb.leitnerbox.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.jb.leitnerbox.core.domain.model.Deck

val DeckColorPalette = Deck.AVAILABLE_COLORS.map { Color(android.graphics.Color.parseColor(it)) }
