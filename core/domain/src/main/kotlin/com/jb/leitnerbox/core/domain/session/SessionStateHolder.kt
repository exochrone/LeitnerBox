package com.jb.leitnerbox.core.domain.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.SessionPlanItem

class SessionStateHolder {
    var pendingCards: List<Card> = emptyList()
    var selectedItems: List<SessionPlanItem> = emptyList()
}
