package com.jb.leitnerbox.feature.session.ui.extra

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.usecase.card.CheckAnswerUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.session.BuildExtraSessionUseCase
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
    private val sessionStateHolder: SessionStateHolder
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

            viewModelScope.launch {
                _events.send(ExtraSessionEvent.SessionFinished)
            }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex   = nextIndex,
                    isFlipped      = false,
                    inputValidated = false,
                    checkResult    = null,
                    userInput      = ""
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
    }
}

sealed class ExtraSessionEvent {
    data object SessionFinished : ExtraSessionEvent()
}
