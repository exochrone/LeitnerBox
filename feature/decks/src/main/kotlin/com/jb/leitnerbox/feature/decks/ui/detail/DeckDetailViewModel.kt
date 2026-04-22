package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    val deck: StateFlow<Deck?> = deckRepository.getDeckById(deckId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val cards: StateFlow<List<Card>> = cardRepository.getCardsByDeckId(deckId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun deleteDeck(onDeleted: (Deck) -> Unit) {
        viewModelScope.launch {
            deck.value?.let {
                val deckToDelete = it
                deckRepository.deleteDeck(deckToDelete)
                onDeleted(deckToDelete)
            }
        }
    }

    fun undoDelete(deck: Deck) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
        }
    }
}
