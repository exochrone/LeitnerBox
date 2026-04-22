package com.jb.leitnerbox.feature.decks.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.usecase.card.AddCardUseCase
import com.jb.leitnerbox.core.domain.usecase.card.DeleteCardUseCase
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BoxDetailViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val addCardUseCase: AddCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])
    private val boxIndex: Int = checkNotNull(savedStateHandle["boxIndex"])
    val boxNumber = boxIndex + 1

    val cards: StateFlow<List<Card>> = getCardsUseCase(deckId)
        .map { list -> list.filter { it.box == boxNumber } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            deleteCardUseCase(card)
        }
    }

    fun undoDelete(card: Card) {
        viewModelScope.launch {
            addCardUseCase(card)
        }
    }
}
