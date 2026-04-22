package com.jb.leitnerbox.core.domain.model

import java.time.Instant

data class SessionPlan(
    val date: Instant,
    val items: List<SessionPlanItem>
)

data class SessionPlanItem(
    val deck: Deck,
    val boxNumber: Int,
    val cardCount: Int
)