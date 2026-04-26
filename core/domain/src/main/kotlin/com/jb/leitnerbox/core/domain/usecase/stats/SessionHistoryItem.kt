package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.model.Session

data class SessionHistoryItem(
    val session: Session,
    val deckNames: List<String>,
    val successRate: Int
)
