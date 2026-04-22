package com.jb.leitnerbox.feature.decks.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.deck.AddDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.DeleteDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
import com.jb.leitnerbox.feature.decks.ui.list.model.DeckDisplayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val getDecksUseCase: GetDecksUseCase,
    private val deleteDeckUseCase: DeleteDeckUseCase,
    private val addDeckUseCase: AddDeckUseCase
) : ViewModel() {

    val decks: StateFlow<List<DeckDisplayItem>> = getDecksUseCase.executeWithCardCount()
        .map { list ->
            list.map { item ->
                DeckDisplayItem(
                    deck = item.deck,
                    cardCount = item.cardCount
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun undoDelete(deck: Deck) {
        viewModelScope.launch {
            addDeckUseCase(deck)
        }
    }
}
