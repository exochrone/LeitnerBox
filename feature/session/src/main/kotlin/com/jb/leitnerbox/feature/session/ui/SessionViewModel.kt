package com.jb.leitnerbox.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val cards = sessionStateHolder.pendingCards
        if (cards.isNotEmpty()) {
            _uiState.update { 
                it.copy(
                    currentCard = cards.first(),
                    totalCards = cards.size,
                    currentIndex = 0
                )
            }
        } else {
            _uiState.update { it.copy(isSessionFinished = true) }
        }
    }

    fun onFlipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun onEvaluate(score: Int) {
        // Logique pour US-06 (Auto-évaluation)
        // Pour l'instant on passe juste à la suivante pour valider le flux
        nextCard()
    }

    private fun nextCard() {
        val nextIndex = _uiState.value.currentIndex + 1
        val cards = sessionStateHolder.pendingCards

        if (nextIndex < cards.size) {
            _uiState.update {
                it.copy(
                    currentCard = cards[nextIndex],
                    currentIndex = nextIndex,
                    isFlipped = false
                )
            }
        } else {
            _uiState.update { it.copy(isSessionFinished = true) }
        }
    }
}
