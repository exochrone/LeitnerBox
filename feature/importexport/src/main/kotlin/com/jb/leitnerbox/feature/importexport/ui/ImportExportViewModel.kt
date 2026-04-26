package com.jb.leitnerbox.feature.importexport.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.csv.ImportResult
import com.jb.leitnerbox.core.domain.csv.MalformedReason
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.GetDeckByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.importexport.ExportDeckUseCase
import com.jb.leitnerbox.core.domain.usecase.importexport.ImportCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckById: GetDeckByIdUseCase,
    private val getCards: GetCardsUseCase,
    private val importCards: ImportCardsUseCase,
    private val exportDeck: ExportDeckUseCase
) : ViewModel() {

    private val deckId: Long = checkNotNull(savedStateHandle["deckId"])

    private val _uiState = MutableStateFlow(ImportExportUiState(targetDeckId = deckId))
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    private val _events = Channel<ImportExportEvent>(Channel.BUFFERED)
    val events: Flow<ImportExportEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                getDeckById(deckId),
                getCards(deckId)
            ) { deck, cards ->
                _uiState.update {
                    it.copy(
                        targetDeckName = deck?.name ?: "",
                        exportCardCount = cards.size
                    )
                }
            }.collect()
        }
    }

    fun onExportClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val csvContent = exportDeck(deckId)
            _uiState.update { it.copy(isLoading = false) }
            val deckName = _uiState.value.targetDeckName
            _events.send(ImportExportEvent.ShareCsv(csvContent, deckName))
        }
    }

    fun onFileContent(csvContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = importCards(csvContent, deckId)
            _uiState.update {
                it.copy(isLoading = false, result = result.toUiResult())
            }
        }
    }

    fun onResultDismissed() {
        _uiState.update { it.copy(result = null) }
    }

    private fun ImportResult.toUiResult(): ImportUiResult = when (this) {
        is ImportResult.Success -> ImportUiResult.Success(importedCount)
        is ImportResult.DuplicatesFound -> ImportUiResult.DuplicatesFound(duplicates)
        is ImportResult.MalformedFile -> when (reason) {
            MalformedReason.MISSING_REQUIRED_COLUMN -> ImportUiResult.MissingColumns
            MalformedReason.ENCODING_ERROR -> ImportUiResult.EncodingError
            MalformedReason.EMPTY_FILE -> ImportUiResult.EmptyFile
            MalformedReason.INVALID_FORMAT -> ImportUiResult.MalformedFile
        }
    }
}

sealed class ImportExportEvent {
    data class ShareCsv(val csvContent: String, val deckName: String) : ImportExportEvent()
}
