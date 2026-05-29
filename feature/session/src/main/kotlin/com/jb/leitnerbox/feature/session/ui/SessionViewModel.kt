package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
import com.jb.leitnerbox.core.domain.usecase.card.EvaluateCardUseCase
import com.jb.leitnerbox.core.domain.usecase.session.SaveSessionUseCase
import com.jb.leitnerbox.core.ui.components.TtsSessionHelper
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
    private val saveSession: SaveSessionUseCase,
    private val cardRepository: CardRepository
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
                    masteredThisSession = 0,
                    isChallenge = sessionStateHolder.isChallengeMode
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
        val state = _uiState.value
        // Utilisation du helper centralisé
        TtsSessionHelper.resolveTextAndSpeak(
            card = state.currentCard,
            isFlipped = state.isFlipped,
            isTtsEnabled = state.isTextToSpeechEnabled,
            onSpeakRequest = ::onSpeakRequest
        )
    }

    fun onToggleFlip() {
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

        // Le déclenchement de la parole se base désormais uniquement sur le nouvel état stabilisé
        val newState = _uiState.value
        TtsSessionHelper.resolveTextAndSpeak(
            card = newState.currentCard,
            isFlipped = newState.isFlipped,
            isTtsEnabled = newState.isTextToSpeechEnabled,
            onSpeakRequest = ::onSpeakRequest
        )
        
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
        val isChallenge = currentState.isChallenge

        viewModelScope.launch {
            if (isChallenge) {
                // In challenge mode, cards stay mastered. We just update UI stats.
                _uiState.update {
                    it.copy(
                        evaluatedCount = it.evaluatedCount + 1,
                        successCount = if (isCorrect) it.successCount + 1 else it.successCount,
                        masteredThisSession = if (isCorrect) it.masteredThisSession + 1 else it.masteredThisSession
                    )
                }
                if (!currentCard.needsInput) {
                    moveToNextCard()
                }
            } else {
                val deck = getDeckForCard(currentCard) ?: return@launch
                val isMastered = isCorrect && currentCard.box == deck.intervals.size
                
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

    fun onSpeakRequest(text: String) {
        viewModelScope.launch {
            _events.send(SessionUiEvent.SpeakText(text))
        }
    }

    fun onZoomChange(isIncrease: Boolean) {
        val currentState = _uiState.value
        val card = currentState.currentCard ?: return
        val step = 0.1f
        
        val updatedCard = if (currentState.isFlipped) {
            val newZoom = if (isIncrease) card.versoZoom + step else card.versoZoom - step
            card.copy(versoZoom = newZoom.coerceIn(0.5f, 3.0f))
        } else {
            val newZoom = if (isIncrease) card.rectoZoom + step else card.rectoZoom - step
            card.copy(rectoZoom = newZoom.coerceIn(0.5f, 3.0f))
        }

        if (updatedCard != card) {
            _uiState.update { state ->
                state.copy(
                    cards = state.cards.map { if (it.id == updatedCard.id) updatedCard else it },
                    currentCard = updatedCard
                )
            }
            viewModelScope.launch {
                cardRepository.updateCard(updatedCard)
            }
        }
    }

    fun toggleTextToSpeech() {
        _uiState.update { it.copy(isTextToSpeechEnabled = !it.isTextToSpeechEnabled) }
        val state = _uiState.value
        if (state.isTextToSpeechEnabled) {
            TtsSessionHelper.resolveTextAndSpeak(
                card = state.currentCard,
                isFlipped = state.isFlipped,
                isTtsEnabled = state.isTextToSpeechEnabled,
                onSpeakRequest = ::onSpeakRequest
            )
        } else {
            onSpeakRequest("") // Stop immédiat du flux audio
        }
    }

    fun onToggleButtons() {
        _uiState.update { it.copy(showButtons = !it.showButtons) }
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
            // Lecture automatique du recto de la nouvelle carte via le helper
            TtsSessionHelper.resolveTextAndSpeak(
                card = nextCard,
                isFlipped = false,
                isTtsEnabled = _uiState.value.isTextToSpeechEnabled,
                onSpeakRequest = ::onSpeakRequest
            )
        }
    }

    private suspend fun onSessionComplete() {
        val state = _uiState.value
        val selectedItems = sessionStateHolder.selectedItems
        
        // Group boxes by deckId
        val deckBoxes = selectedItems
            .groupBy { it.deck.id }
            .mapValues { (_, items) -> items.map { it.boxNumber }.distinct().sorted() }

        val session = Session(
            date = Instant.now(),
            deckIds = selectedItems.map { it.deck.id }.distinct(),
            cardCount = state.cards.size,
            successCount = state.successCount,
            masteredCount = if (state.isChallenge) 0 else state.masteredThisSession, // No new mastery in challenge
            advancedCount = state.advancedCount,
            retreatedCount = state.retreatedCount,
            isReported = false,
            deckBoxes = deckBoxes
        )
        // We might choose NOT to save challenge sessions to the history
        // to avoid skewing stats. Roadmap doesn't explicitly say.
        // I'll save it for now, as it's activity.
        saveSession(session)

        // Reset challenge mode flag
        sessionStateHolder.isChallengeMode = false

        // Stocker le résultat pour l'écran suivant
        sessionStateHolder.lastSessionResult = session
        sessionStateHolder.isExtraSession = false
        _events.send(SessionUiEvent.SessionFinished)
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}
