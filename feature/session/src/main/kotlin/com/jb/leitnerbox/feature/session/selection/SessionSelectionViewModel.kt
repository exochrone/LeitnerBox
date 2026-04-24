package com.jb.leitnerbox.feature.session.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.session.BuildSessionUseCase
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SessionSelectionEvent {
    object NavigateToSession : SessionSelectionEvent()
}

@HiltViewModel
class SessionSelectionViewModel @Inject constructor(
    private val getDailySessionPlan: GetDailySessionPlanUseCase,
    private val buildSession: BuildSessionUseCase,
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSelectionUiState())
    val uiState: StateFlow<SessionSelectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SessionSelectionEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getDailySessionPlan().collect { plan ->
                _uiState.update {
                    it.copy(
                        items = plan.items.map { item -> SelectableBoxItem(item) },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onBoxToggled(item: SelectableBoxItem) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.planItem == item.planItem) it.copy(isSelected = !it.isSelected) else it
            })
        }
    }

    fun onStartSession() {
        viewModelScope.launch {
            val selected = _uiState.value.items
                .filter { it.isSelected }
                .map { it.planItem }
            
            if (selected.isNotEmpty()) {
                val cards = buildSession(selected)
                sessionStateHolder.pendingCards = cards
                sessionStateHolder.selectedItems = selected
                _events.emit(SessionSelectionEvent.NavigateToSession)
            }
        }
    }
}
