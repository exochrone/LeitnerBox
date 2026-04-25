package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
import com.jb.leitnerbox.core.domain.usecase.card.EvaluateCardUseCase
import com.jb.leitnerbox.core.domain.usecase.session.SaveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder,
    private val evaluateCard: EvaluateCardUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val saveSession: SaveSessionUseCase
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
            val firstCard = cards.first()
            _uiState.update { 
                it.copy(
                    cards = cards,
                    currentCard = firstCard,
                    currentDeckName = getDeckForCard(firstCard)?.name ?: "",
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
                    masteredThisSession = if (isMastered) it.masteredThisSession + 1 else it.masteredThisSession,
                    isMasteredTransition = isMastered
                )
            }

            if (isMastered) {
                _events.send(SessionUiEvent.CardMastered)
            } else if (!currentCard.needsInput) {
                moveToNextCard()
            }
        }
    }

    fun onMasteryCelebrationFinished() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(250)
            _uiState.update { it.copy(isMasteredTransition = false) }
            moveToNextCard()
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
                onSessionComplete()
            }
        } else {
            val nextCard = cards[nextIndex]
            _uiState.update {
                it.copy(
                    currentCard = nextCard,
                    currentDeckName = getDeckForCard(nextCard)?.name ?: "",
                    currentIndex = nextIndex,
                    isFlipped = false,
                    userInput = "",
                    inputValidated = false,
                    checkResult = null
                )
            }
        }
    }

    private suspend fun onSessionComplete() {
        android.util.Log.d("SessionVM", "onSessionComplete called — saving session")
        val state = _uiState.value
        val session = Session(
            date = Instant.now(),
            deckIds = sessionStateHolder.selectedItems.map { it.deck.id }.distinct(),
            cardCount = state.cards.size,
            successCount = state.successCount,
            masteredCount = state.masteredThisSession,
            advancedCount = state.advancedCount,
            retreatedCount = state.retreatedCount,
            isReported = false
        )
        saveSession(session)
        android.util.Log.d("SessionVM", "Session saved")

        // Stocker le résultat pour l'écran suivant
        sessionStateHolder.lastSessionResult = session
        _events.send(SessionUiEvent.SessionFinished)
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
