package com.jb.leitnerbox.feature.cards.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.usecase.card.GetCardByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.card.UpdateCardUseCase
import com.jb.leitnerbox.core.domain.utils.LatexDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardUpdateViewModel @Inject constructor(
    private val getCardByIdUseCase: GetCardByIdUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: Long = checkNotNull(savedStateHandle["cardId"])

    private val _uiState = MutableStateFlow(CardEditUiState())
    val uiState: StateFlow<CardEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CardEditEvent>()
    val events = _events.asSharedFlow()

    private var originalCard: Card? = null

    init {
        viewModelScope.launch {
            getCardByIdUseCase(cardId).collect { card ->
                card?.let {
                    originalCard = it
                    _uiState.update { state ->
                        state.copy(
                            recto = it.recto,
                            verso = it.verso,
                            needsInput = it.needsInput
                        )
                    }
                }
            }
        }
    }

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

    fun saveCard() {
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
            _uiState.update { it.copy(isLoading = true) }
            
            originalCard?.let {
                val updatedCard = it.copy(
                    recto = recto,
                    verso = verso,
                    needsInput = needsInput
                )
                updateCardUseCase(updatedCard)
                _events.emit(CardEditEvent.CardSaved)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}