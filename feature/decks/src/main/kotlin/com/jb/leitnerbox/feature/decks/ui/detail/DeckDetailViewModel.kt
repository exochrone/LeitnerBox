package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val getDeckByIdUseCase: GetDeckByIdUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val getDeckSummary: GetDeckSummaryUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val updateDeckColorUseCase: UpdateDeckColorUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    val uiState: StateFlow<DeckDetailUiState> = getDeckByIdUseCase(deckId)
        .flatMapLatest { deck ->
            if (deck == null) {
                flowOf(DeckDetailUiState(isLoading = false))
            } else {
                combine(
                    getCardsUseCase(deckId),
                    getDeckSummary(deckId, deck.intervals.size)
                ) { cards, summary ->
                    DeckDetailUiState(
                        deck = deck,
                        cards = cards,
                        progress = summary.progress,
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeckDetailUiState(isLoading = true)
        )

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

    fun onColorSelected(colorHex: String) {
        viewModelScope.launch {
            uiState.value.deck?.let { deck ->
                updateDeckColorUseCase(deck, colorHex)
            }
        }
    }

    fun renameDeck(newName: String) {
        viewModelScope.launch {
            uiState.value.deck?.let { deck ->
                if (newName.isNotBlank() && newName != deck.name) {
                    updateDeckUseCase(deck.copy(name = newName.trim()))
                }
            }
        }
    }
}
