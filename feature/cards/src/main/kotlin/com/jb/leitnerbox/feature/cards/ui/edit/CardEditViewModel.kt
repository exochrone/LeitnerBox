package com.jb.leitnerbox.feature.cards.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.usecase.card.AddCardUseCase
import com.jb.leitnerbox.core.domain.utils.LatexDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardEditUiState(
    val recto: String = "",
    val verso: String = "",
    val needsInput: Boolean = false,
    val rectoError: Boolean = false,
    val versoError: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

sealed class CardEditEvent {
    object CardSaved : CardEditEvent()
    object CardSavedAndNext : CardEditEvent()
}

@HiltViewModel
class CardEditViewModel @Inject constructor(
    private val addCardUseCase: AddCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _uiState = MutableStateFlow(CardEditUiState())
    val uiState: StateFlow<CardEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CardEditEvent>()
    val events = _events.asSharedFlow()

    fun onRectoChange(text: String) {
        _uiState.update { it.copy(recto = text, rectoError = false) }
    }

    fun onVersoChange(text: String) {
        val containsLatex = LatexDetector.containsLatex(text)
        _uiState.update { 
            it.copy(
                verso = text, 
                versoError = false,
                needsInput = if (containsLatex) false else it.needsInput
            ) 
        }
    }

    fun onNeedsInputChange(needsInput: Boolean) {
        _uiState.update { it.copy(needsInput = needsInput) }
    }

    fun saveCard(andNext: Boolean = false) {
        val recto = _uiState.value.recto.trim()
        val verso = _uiState.value.verso.trim()
        val needsInput = _uiState.value.needsInput
        
        var hasError = false
        if (recto.isEmpty()) {
            _uiState.update { it.copy(rectoError = true) }
            hasError = true
        }
        if (verso.isEmpty()) {
            _uiState.update { it.copy(versoError = true) }
            hasError = true
        }
        
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val card = Card(
                deckId = deckId,
                recto = recto,
                verso = verso,
                box = 1,
                needsInput = needsInput
            )
            
            addCardUseCase(card).onSuccess {
                if (andNext) {
                    _uiState.update { 
                        it.copy(
                            recto = "",
                            verso = "",
                            needsInput = false,
                            isLoading = false
                        )
                    }
                    _events.emit(CardEditEvent.CardSavedAndNext)
                } else {
                    _events.emit(CardEditEvent.CardSaved)
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure { error ->
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = if (error is IllegalStateException) "Cette carte existe déjà dans ce deck" else error.message
                    )
                }
            }
        }
    }
}
