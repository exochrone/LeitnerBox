package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoxDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])
    private val boxIndex: Int = checkNotNull(savedStateHandle["boxIndex"])
    val boxNumber = boxIndex + 1

    val cards: StateFlow<List<Card>> = cardRepository.getCardsByDeckId(deckId)
        .map { list -> list.filter { it.box == boxNumber } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            cardRepository.deleteCard(card)
        }
    }

    fun undoDelete(card: Card) {
        viewModelScope.launch {
            cardRepository.insertCard(card)
        }
    }
}