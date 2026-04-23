package com.jb.leitnerbox.feature.session.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionSelectionViewModel @Inject constructor(
    private val getDailySessionPlan: GetDailySessionPlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionSelectionUiState())
    val uiState: StateFlow<SessionSelectionUiState> = _uiState.asStateFlow()

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
}
