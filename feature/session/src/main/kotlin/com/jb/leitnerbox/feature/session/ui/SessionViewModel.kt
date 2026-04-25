package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
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
                    checkResult = null,
                    evaluatedCount = 0,
                    successCount = 0,
                    advancedCount = 0,
                    retreatedCount = 0,
                    masteredThisSession = 0
                )
            }
        } else {
            viewModelScope.launch {
                _events.send(SessionUiEvent.SessionFinished)
            }
        }
    }

    fun onFlipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
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

    private fun getDeckForCard(card: Card): Deck? {
        return sessionStateHolder.selectedItems
            .firstOrNull { it.deck.id == card.deckId }
            ?.deck
    }

    fun onEvaluate(isCorrect: Boolean) {
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return
        val deck = getDeckForCard(currentCard) ?: return
        val isMastered = isCorrect && currentCard.box == deck.intervals.size

        viewModelScope.launch {
            evaluateCard(currentCard, deck, isCorrect)
            
            _uiState.update {
                it.copy(
                    evaluatedCount = it.evaluatedCount + 1,
                    successCount = if (isCorrect) it.successCount + 1 else it.successCount,
                    advancedCount = if (isCorrect && !isMastered) it.advancedCount + 1 else it.advancedCount,
                    retreatedCount = if (!isCorrect && currentCard.box > 1) it.retreatedCount + 1 else it.retreatedCount,
                    masteredThisSession = if (isMastered) it.masteredThisSession + 1 else it.masteredThisSession
                )
            }

            if (isMastered) {
                _events.send(SessionUiEvent.CardMastered)
            }

            if (!currentCard.needsInput) {
                moveToNextCard()
            }
        }
    }

    fun onContinue() {
        moveToNextCard()
    }

    private fun moveToNextCard() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        val cards = state.cards

        if (nextIndex >= cards.size) {
            viewModelScope.launch {
                _events.send(SessionUiEvent.SessionFinished)
            }
        } else {
            _uiState.update {
                it.copy(
                    currentCard = cards[nextIndex],
                    currentIndex = nextIndex,
                    isFlipped = false,
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
