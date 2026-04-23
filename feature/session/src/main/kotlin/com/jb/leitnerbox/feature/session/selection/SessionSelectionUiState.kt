package com.jb.leitnerbox.feature.session.selection

import com.jb.leitnerbox.core.domain.model.SessionPlanItem

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
