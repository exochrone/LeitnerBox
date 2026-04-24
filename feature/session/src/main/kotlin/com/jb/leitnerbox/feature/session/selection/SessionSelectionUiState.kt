package com.jb.leitnerbox.feature.session.selection

import com.jb.leitnerbox.core.domain.model.SessionPlanItem

sealed class SessionSelectionEvent {
    object NavigateToSession : SessionSelectionEvent()
}

data class SessionSelectionUiState(
    val items: List<SelectableBoxItem> = emptyList(),
    val isLoading: Boolean = true
)

data class SelectableBoxItem(
    val planItem: SessionPlanItem,
    val isSelected: Boolean = true
)

val SessionSelectionUiState.totalSelectedCards: Int
    get() = items.filter { it.isSelected }.sumOf { it.planItem.cardCount }

val SessionSelectionUiState.canStart: Boolean
    get() = items.any { it.isSelected }
