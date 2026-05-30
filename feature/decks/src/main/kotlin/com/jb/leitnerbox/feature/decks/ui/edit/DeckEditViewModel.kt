package com.jb.leitnerbox.feature.decks.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.UpdateDeckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckEditUiState(
    val name: String = "",
    val nameError: Boolean = false,
    val presentationOrder: PresentationOrder = PresentationOrder.BY_BOX,
    val wrongAnswerRule: WrongAnswerRule = WrongAnswerRule.PREVIOUS_BOX,
    val color: String = Deck.DEFAULT_COLOR,
    val boxCount: Int = 5,
    val intervals: List<String> = listOf("1", "3", "5", "7", "14"),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val showExitConfirm: Boolean = false,
    val showBoxReductionConfirm: Boolean = false,
    val disappearingBoxes: List<Int> = emptyList(),
    val targetBox: Int = 0
)

sealed class DeckEditEvent {
    object DeckSaved : DeckEditEvent()
    object ExitWizard : DeckEditEvent()
}

@HiltViewModel
class DeckEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val deckId: Long? = savedStateHandle.get<Long>("deckId")
    private var initialBoxCount: Int = 0

    private val _uiState = MutableStateFlow(DeckEditUiState())
    val uiState: StateFlow<DeckEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DeckEditEvent>()
    val events = _events.asSharedFlow()

    private val defaultIntervals = listOf(1, 3, 5, 7, 14, 30, 60)

    init {
        deckId?.let { id ->
            if (id > 0) {
                viewModelScope.launch {
                    getDeckByIdUseCase(id).firstOrNull()?.let { deck ->
                        initialBoxCount = deck.intervals.size
                        _uiState.update {
                            it.copy(
                                name = deck.name,
                                presentationOrder = deck.presentationOrder,
                                wrongAnswerRule = deck.wrongAnswerRule,
                                color = deck.color,
                                boxCount = deck.intervals.size,
                                intervals = deck.intervals.map { i -> i.toString() },
                                isEditing = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = false) }
    }

    fun onPresentationOrderChange(order: PresentationOrder) {
        _uiState.update { it.copy(presentationOrder = order) }
    }

    fun onWrongAnswerRuleChange(rule: WrongAnswerRule) {
        _uiState.update { it.copy(wrongAnswerRule = rule) }
    }

    fun onColorSelected(hex: String) {
        _uiState.update { it.copy(color = hex) }
    }

    fun onBoxCountChange(count: Int) {
        val newIntervals = defaultIntervals.take(count).map { it.toString() }
        _uiState.update { it.copy(boxCount = count, intervals = newIntervals) }
    }

    fun onIntervalChange(index: Int, value: String) {
        val filtered = value.filter { it.isDigit() }.take(3)
        val num = filtered.toIntOrNull() ?: 0
        val finalValue = if (num > 999) "999" else filtered
        
        _uiState.update { state ->
            val newList = state.intervals.toMutableList()
            newList[index] = finalValue
            state.copy(intervals = newList)
        }
    }

    fun onBackRequest() {
        _uiState.update { it.copy(showExitConfirm = true) }
    }

    fun onDismissExitConfirm() {
        _uiState.update { it.copy(showExitConfirm = false) }
    }

    fun onConfirmExit() {
        viewModelScope.launch {
            _events.emit(DeckEditEvent.ExitWizard)
        }
    }

    fun saveDeck() {
        if (_uiState.value.name.trim().isEmpty()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        val newBoxCount = _uiState.value.boxCount
        if (_uiState.value.isEditing && newBoxCount < initialBoxCount) {
            viewModelScope.launch {
                val cards = getCardsUseCase(deckId!!).first()
                val disappearingBoxes = (newBoxCount + 1..initialBoxCount).toList()
                val cardsInDisappearingBoxes = cards.filter { it.box in disappearingBoxes }
                
                if (cardsInDisappearingBoxes.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            showBoxReductionConfirm = true,
                            disappearingBoxes = disappearingBoxes,
                            targetBox = newBoxCount
                        )
                    }
                    return@launch
                }
                performSave()
            }
        } else {
            performSave()
        }
    }

    fun onConfirmBoxReduction() {
        _uiState.update { it.copy(showBoxReductionConfirm = false) }
        viewModelScope.launch {
            val cards = getCardsUseCase(deckId!!).first()
            val disappearingBoxes = _uiState.value.disappearingBoxes
            val targetBox = _uiState.value.targetBox
            
            val cardsToMove = cards.filter { it.box in disappearingBoxes }
            if (cardsToMove.isNotEmpty()) {
                val updatedCards = cardsToMove.map { it.copy(box = targetBox) }
                cardRepository.updateCards(updatedCards)
            }
            performSave()
        }
    }

    fun onDismissBoxReduction() {
        _uiState.update { it.copy(showBoxReductionConfirm = false) }
    }

    private fun performSave() {
        val name = _uiState.value.name.trim()
        val intervals = _uiState.value.intervals.map { it.toIntOrNull() ?: 1 }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val deck = Deck(
                name = name,
                intervals = intervals,
                wrongAnswerRule = _uiState.value.wrongAnswerRule,
                presentationOrder = _uiState.value.presentationOrder,
                color = _uiState.value.color
            )
            
            if (_uiState.value.isEditing && deckId != null && deckId > 0) {
                updateDeckUseCase(deck.copy(id = deckId))
            } else {
                addDeckUseCase(deck.copy(id = 0L))
            }

            _events.emit(DeckEditEvent.DeckSaved)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
