package com.jb.leitnerbox.feature.challenge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.GetMasteredCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val getMasteredCards: GetMasteredCardsUseCase,
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        getMasteredCards()
            .onEach { cards ->
                _uiState.update { it.copy(masteredCount = cards.size, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun startChallenge(onNavigate: () -> Unit) {
        viewModelScope.launch {
            val cards = getMasteredCards().first().shuffled()
            if (cards.isNotEmpty()) {
                sessionStateHolder.pendingCards = cards
                sessionStateHolder.selectedItems = emptyList() // No specific deck selection for challenge
                sessionStateHolder.isChallengeMode = true
                onNavigate()
            }
        }
    }
}
