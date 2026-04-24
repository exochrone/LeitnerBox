package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SessionUiEvent {
    object SessionFinished : SessionUiEvent()
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SessionUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val cards = sessionStateHolder.pendingCards
        if (cards.isNotEmpty()) {
            _uiState.update { 
                it.copy(
                    currentCard = cards.first(),
                    totalCards = cards.size,
                    currentIndex = 0,
                    isFlipped = false
                )
            }
        } else {
            viewModelScope.launch {
                _events.send(SessionUiEvent.SessionFinished)
            }
        }
    }

    fun onFlipCard() {
        _uiState.update { it.copy(isFlipped = true) }
    }

    fun onEvaluate(isCorrect: Boolean) {
        // Logique pour US-06 (Auto-évaluation)
        nextCard()
    }

    private fun nextCard() {
        val nextIndex = _uiState.value.currentIndex + 1
        val cards = sessionStateHolder.pendingCards

        if (nextIndex < cards.size) {
            _uiState.update {
                it.copy(
                    currentCard = cards[nextIndex],
                    currentIndex = nextIndex,
                    isFlipped = false
                )
            }
        } else {
            viewModelScope.launch {
                _events.send(SessionUiEvent.SessionFinished)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
