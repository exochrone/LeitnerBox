package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.EvaluateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder,
    private val evaluateCard: EvaluateCardUseCase
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
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return
        
        val deck = sessionStateHolder.selectedItems
            .firstOrNull { it.deck.id == currentCard.deckId }
            ?.deck ?: return

        viewModelScope.launch {
            evaluateCard(currentCard, deck, isCorrect)
            moveToNextCard(isCorrect)
        }
    }

    private fun moveToNextCard(isCorrect: Boolean) {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        val cards = sessionStateHolder.pendingCards

        if (nextIndex >= cards.size) {
            _uiState.update {
                it.copy(
                    evaluatedCount = it.evaluatedCount + 1,
                    successCount = if (isCorrect) it.successCount + 1 else it.successCount
                )
            }
            viewModelScope.launch {
                _events.send(SessionUiEvent.SessionFinished)
            }
        } else {
            _uiState.update {
                it.copy(
                    currentCard = cards[nextIndex],
                    currentIndex = nextIndex,
                    isFlipped = false,
                    evaluatedCount = it.evaluatedCount + 1,
                    successCount = if (isCorrect) it.successCount + 1 else it.successCount
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
