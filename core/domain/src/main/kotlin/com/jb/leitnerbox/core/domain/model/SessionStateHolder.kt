package com.jb.leitnerbox.core.domain.model

class SessionStateHolder {
    var pendingCards: List<Card> = emptyList()
    var selectedItems: List<SessionPlanItem> = emptyList()
}
