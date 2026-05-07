package com.jb.leitnerbox.feature.decks.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.card.InsertCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckSummaryUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.RestoreDeckUseCase
import com.jb.leitnerbox.feature.decks.ui.list.model.DeckDisplayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val getDecksUseCase: GetDecksUseCase,
    private val getDeckSummary: GetDeckSummaryUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    private val restoreDeckUseCase: RestoreDeckUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val insertCardsUseCase: InsertCardsUseCase
) : ViewModel() {

    private var deletedDeckCards: List<Card> = emptyList()

    val decks: StateFlow<List<DeckDisplayItem>> = getDecksUseCase.executeWithCardCount()
        .flatMapLatest { items ->
            if (items.isEmpty()) {
                flowOf(emptyList())
            } else {
                val summaryFlows = items.map { item ->
                    getDeckSummary(item.deck.id, item.deck.intervals.size)
                        .map { summary ->
                            DeckDisplayItem(
                                deck           = item.deck,
                                totalCardCount = item.cardCount,
                                masteredCount  = summary.masteredCount,
                                cardsPerBox    = summary.cardsPerBox,
                                nextReviewDate = summary.nextReviewDate,
                                progress       = summary.progress
                            )
                        }
                }
                combine(summaryFlows) { displayItems ->
                    displayItems.sortedBy { it.deck.name }
                }
            }
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            val cards = getCardsUseCase(deck.id).first()
            deleteDeckUseCase(deck)
            deletedDeckCards = cards
        }
    }

    fun undoDelete(deck: Deck, cards: List<Card>? = null) {
        viewModelScope.launch {
            val cardsToRestore = cards ?: deletedDeckCards
            restoreDeckUseCase(deck)
            if (cardsToRestore.isNotEmpty()) {
                insertCardsUseCase(cardsToRestore)
            }
            deletedDeckCards = emptyList()
        }
    }

    fun onDeleteConfirmed() {
        deletedDeckCards = emptyList()
    }
}
