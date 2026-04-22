package com.jb.leitnerbox.feature.decks.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckEditUiState(
    val name: String = "",
    val nameError: Boolean = false,
    val wrongAnswerRule: WrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
    val isLoading: Boolean = false
)

sealed class DeckEditEvent {
    object DeckSaved : DeckEditEvent()
}

@HiltViewModel
class DeckEditViewModel @Inject constructor(
    private val addDeckUseCase: AddDeckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckEditUiState())
    val uiState: StateFlow<DeckEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DeckEditEvent>()
    val events = _events.asSharedFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = false) }
    }

    fun onWrongAnswerRuleChange(rule: WrongAnswerRule) {
        _uiState.update { it.copy(wrongAnswerRule = rule) }
    }

    fun saveDeck() {
        val name = _uiState.value.name.trim()
        val wrongAnswerRule = _uiState.value.wrongAnswerRule
        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Version figée pour P3-UI-10 : 5 boîtes (1, 3, 5, 7, 14 jours)
            val defaultIntervals = listOf(1, 3, 5, 7, 14)
            
            val deck = Deck(
                name = name,
                intervals = defaultIntervals,
                wrongAnswerRule = wrongAnswerRule
            )
            
            addDeckUseCase(deck)
            _events.emit(DeckEditEvent.DeckSaved)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
