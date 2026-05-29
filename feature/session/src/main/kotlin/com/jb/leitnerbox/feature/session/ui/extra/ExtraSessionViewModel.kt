package com.jb.leitnerbox.feature.session.ui.extra

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.session.SessionType
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.session.BuildExtraSessionUseCase
import com.jb.leitnerbox.core.ui.components.TtsSessionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExtraSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val buildExtraSession: BuildExtraSessionUseCase,
    private val getDeckById: GetDeckByIdUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val sessionStateHolder: SessionStateHolder,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _uiState = MutableStateFlow(ExtraSessionUiState())
    val uiState: StateFlow<ExtraSessionUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExtraSessionEvent>(Channel.BUFFERED)
    val events: Flow<ExtraSessionEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val cards = buildExtraSession(deckId)
            val deck = getDeckById(deckId).first()
            _uiState.update { it.copy(
                cards = cards, 
                currentDeckName = deck?.name ?: "",
                isLoading = false
            ) }
        }
    }

    fun onFlipCard() {
        _uiState.update { it.copy(isFlipped = true) }
        val state = _uiState.value
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

    fun onEvaluate(isCorrect: Boolean) {
        _uiState.update {
            it.copy(
                evaluatedCount = it.evaluatedCount + 1,
                successCount   = if (isCorrect) it.successCount + 1 else it.successCount
            )
        }
        moveToNextCard()
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(userInput = text) }
    }

    fun onInputValidated() {
        val state = _uiState.value
        val card  = state.currentCard ?: return
        val result    = checkAnswer(card, state.userInput)
        val isCorrect = result is AnswerCheckResult.Correct
        _uiState.update {
            it.copy(
                inputValidated = true,
                checkResult    = result,
                isFlipped      = true,
                evaluatedCount = it.evaluatedCount + 1,
                successCount   = if (isCorrect) it.successCount + 1 else it.successCount
            )
        }

        val newState = _uiState.value
        TtsSessionHelper.resolveTextAndSpeak(
            card = newState.currentCard,
            isFlipped = newState.isFlipped,
            isTtsEnabled = newState.isTextToSpeechEnabled,
            onSpeakRequest = ::onSpeakRequest
        )
    }

    fun onContinue() {
        _uiState.update {
            it.copy(
                inputValidated = false,
                checkResult    = null,
                userInput      = ""
            )
        }
        moveToNextCard()
    }

    fun onSpeakRequest(text: String) {
        viewModelScope.launch {
            _events.send(ExtraSessionEvent.SpeakText(text))
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
                    cards = state.cards.map { if (it.id == updatedCard.id) updatedCard else it }
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
            onSpeakRequest("")
        }
    }

    fun onToggleButtons() {
        _uiState.update { it.copy(showButtons = !it.showButtons) }
    }

    private fun moveToNextCard() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.cards.size) {
            val fakeSession = Session(
                id = 0,
                date = java.time.Instant.now(),
                deckIds = listOf(deckId),
                cardCount = state.evaluatedCount,
                successCount = state.successCount,
                masteredCount = 0,
                advancedCount = 0,
                retreatedCount = 0,
                isReported = false
            )
            sessionStateHolder.lastSessionResult = fakeSession
            sessionStateHolder.isExtraSession = true
            sessionStateHolder.lastSessionType = SessionType.EXTRA

            viewModelScope.launch {
                _events.send(ExtraSessionEvent.SessionFinished)
            }
        } else {
            val nextCard = state.cards[nextIndex]
            _uiState.update {
                it.copy(
                    currentIndex   = nextIndex,
                    isFlipped      = false,
                    inputValidated = false,
                    checkResult    = null,
                    userInput      = ""
                )
            }
            TtsSessionHelper.resolveTextAndSpeak(
                card = nextCard,
                isFlipped = false,
                isTtsEnabled = _uiState.value.isTextToSpeechEnabled,
                onSpeakRequest = ::onSpeakRequest
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}

sealed class ExtraSessionEvent {
    data object SessionFinished : ExtraSessionEvent()
    data class SpeakText(val text: String) : ExtraSessionEvent()
}
