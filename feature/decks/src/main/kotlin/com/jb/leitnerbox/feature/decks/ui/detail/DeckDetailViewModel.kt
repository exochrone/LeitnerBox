package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
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
    private val updateDeckUseCase: UpdateDeckUseCase,
    private val updateDeckColorUseCase: UpdateDeckColorUseCase,
    private val activateManualCardsUseCase: com.jb.leitnerbox.core.domain.usecase.tampon.ActivateManualCardsUseCase,
    private val settingsRepository: SettingsRepository,
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
                    getDeckSummary(deckId, deck.intervals.size),
                    settingsRepository.getNewCardsPerDay()
                ) { cards, summary, newCardsPerDay ->
                    DeckDetailUiState(
                        deck = deck,
                        cards = cards,
                        inactiveCardCount = summary.inactiveCount,
                        newCardsPerDay = newCardsPerDay,
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

    fun deleteDeck(onDeleted: (Deck, List<Card>) -> Unit) {
        viewModelScope.launch {
            uiState.value.deck?.let { deck ->
                val cards = uiState.value.cards
                deleteDeckUseCase(deck)
                onDeleted(deck, cards)
            }
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

    fun activateCards(count: Int) {
        viewModelScope.launch {
            activateManualCardsUseCase(deckId, count)
        }
    }
}
