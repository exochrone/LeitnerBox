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

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    val uiState: StateFlow<DeckDetailUiState> = combine(
        getDeckByIdUseCase(deckId),
        getCardsUseCase(deckId)
    ) { deck, cards ->
        if (deck == null) {
            DeckDetailUiState(isLoading = false)
        } else {
            val progress = computeProgress(cards, deck.intervals.size)
            DeckDetailUiState(
                deck = deck,
                cards = cards,
                progress = progress,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeckDetailUiState(isLoading = true)
    )

    private fun computeProgress(cards: List<Card>, boxCount: Int): Float {
        if (cards.isEmpty()) return 0f
        val totalScore = cards.sumOf { card ->
            if (card.isLearned) boxCount.toDouble() else (card.box - 1).toDouble()
        }
        return (totalScore / (cards.size * boxCount)).toFloat()
    }

    fun deleteDeck(onDeleted: (Deck) -> Unit) {
        viewModelScope.launch {
            uiState.value.deck?.let {
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
