package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.usecase.deck.GetDecksUseCase
import com.jb.leitnerbox.core.domain.usecase.importexport.ExportDecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CsvExportViewModel @Inject constructor(
    private val getDecks: GetDecksUseCase,
    private val exportDecks: ExportDecksUseCase
) : ViewModel() {

    val decks: StateFlow<List<Deck>> = getDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDeckIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedDeckIds: StateFlow<Set<Long>> = _selectedDeckIds.asStateFlow()

    private val _uiState = MutableStateFlow(CsvExportUiState())
    val uiState: StateFlow<CsvExportUiState> = _uiState.asStateFlow()

    private val _events = Channel<CsvExportEvent>(Channel.BUFFERED)
    val events: Flow<CsvExportEvent> = _events.receiveAsFlow()

    fun onDeckToggled(deckId: Long) {
        _selectedDeckIds.update { current ->
            if (deckId in current) current - deckId else current + deckId
        }
    }

    fun onExportClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val selectedDecks = decks.value.filter { it.id in _selectedDeckIds.value }
            val result = exportDecks(selectedDecks)
            val today  = LocalDate.now()
            val fileName = "LeitnerBox-${result.deckCount}_decks-${
                today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            }.csv"
            
            _uiState.update { it.copy(
                isLoading = false,
                pendingCsvContent = result.csvContent,
                lastExportInfo = result.cardCount to result.deckCount
            ) }
            
            _events.send(CsvExportEvent.RequestCreateFile(fileName))
        }
    }

    fun onFileCreated(uri: android.net.Uri, contentResolver: android.content.ContentResolver) {
        val content = _uiState.value.pendingCsvContent ?: return
        viewModelScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { 
                    it.write(content.toByteArray(Charsets.UTF_8))
                }
                _uiState.update { it.copy(showSuccess = true, pendingCsvContent = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDismissSuccess() {
        _uiState.update { it.copy(showSuccess = false, lastExportInfo = null) }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CsvExportUiState(
    val isLoading: Boolean = false,
    val pendingCsvContent: String? = null,
    val lastExportInfo: Pair<Int, Int>? = null, // cardCount to deckCount
    val showSuccess: Boolean = false,
    val error: String? = null
)

sealed class CsvExportEvent {
    data class RequestCreateFile(val fileName: String) : CsvExportEvent()
}
