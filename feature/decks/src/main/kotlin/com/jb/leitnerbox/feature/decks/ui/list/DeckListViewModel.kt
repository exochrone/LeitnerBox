package com.jb.leitnerbox.feature.decks.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckProgressUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
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
    private val getDeckProgress: GetDeckProgressUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val addDeckUseCase: AddDeckUseCase
) : ViewModel() {

    val decks: StateFlow<List<DeckDisplayItem>> = getDecksUseCase.executeWithCardCount()
        .flatMapLatest { items ->
            if (items.isEmpty()) {
                flowOf(emptyList())
            } else {
                val progressFlows = items.map { item ->
                    getDeckProgress(item.deck.id, item.deck.intervals.size)
                        .map { progress ->
                            DeckDisplayItem(
                                deck = item.deck,
                                cardCount = item.cardCount,
                                progress = progress
                            )
                        }
                }
                combine(progressFlows) { it.toList() }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun undoDelete(deck: Deck) {
        viewModelScope.launch {
            addDeckUseCase(deck)
        }
    }
}
