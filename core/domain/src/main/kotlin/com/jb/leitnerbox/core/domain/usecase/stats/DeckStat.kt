package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.model.Deck
import java.time.LocalDate

data class DeckStat(
    val deck: Deck,
    val totalCards: Int,
    val masteredCards: Int,
    val cardsByBox: Map<Int, Int>,
    val successRate: Int?,
    val lastSessionDate: LocalDate?
)
