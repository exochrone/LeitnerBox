package com.jb.leitnerbox.core.domain.usecase.stats

import com.jb.leitnerbox.core.domain.model.Session

data class SessionHistoryItem(
    val session: Session,
    val decksDescription: String,
    val successRate: Int
)
