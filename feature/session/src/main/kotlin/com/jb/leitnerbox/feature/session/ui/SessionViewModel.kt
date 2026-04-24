package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
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
    private val evaluateCard: EvaluateCardUseCase,
    private val checkAnswer: CheckAnswerUseCase
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
                    cards = cards,
                    currentCard = cards.first(),
                    currentIndex = 0,
                    isFlipped = false,
                    userInput = "",
                    inputValidated = false,
                    checkResult = null
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

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(userInput = text) }
    }

    fun onInputValidated() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        val result = checkAnswer(card, state.userInput)
        val isCorrect = result is AnswerCheckResult.Correct
        
        _uiState.update {
            it.copy(
                inputValidated = true,
                checkResult = result,
                isFlipped = true
            )
        }
        
        // On déclenche l'évaluation métier
        onEvaluate(isCorrect)
    }

    fun onEvaluate(isCorrect: Boolean) {
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return
        
        val deck = sessionStateHolder.selectedItems
            .firstOrNull { it.deck.id == currentCard.deckId }
            ?.deck ?: return

        viewModelScope.launch {
            evaluateCard(currentCard, deck, isCorrect)
            if (!currentCard.needsInput) {
                moveToNextCard(isCorrect)
            }
        }
    }

    fun onContinue() {
        val state = _uiState.value
        val isCorrect = state.checkResult is AnswerCheckResult.Correct
        moveToNextCard(isCorrect)
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
                    successCount = if (isCorrect) it.successCount + 1 else it.successCount,
                    userInput = "",
                    inputValidated = false,
                    checkResult = null
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
