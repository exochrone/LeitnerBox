package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    val deck: StateFlow<Deck?> = getDeckByIdUseCase(deckId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val cards: StateFlow<List<Card>> = getCardsUseCase(deckId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun deleteDeck(onDeleted: (Deck) -> Unit) {
        viewModelScope.launch {
            deck.value?.let {
                val deckToDelete = it
                deleteDeckUseCase(deckToDelete)
                onDeleted(deckToDelete)
            }
        }
    }

    fun undoDelete(deck: Deck) {
        viewModelScope.launch {
            addDeckUseCase(deck)
        }
    }
}
