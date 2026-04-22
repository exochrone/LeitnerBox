package com.jb.leitnerbox.feature.decks.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {

    val decks: StateFlow<List<Deck>> = deckRepository.getDecksWithCardCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun undoDelete(deck: Deck) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
        }
    }
}
