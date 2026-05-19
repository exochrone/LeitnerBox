package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.csv.MalformedReason
import com.jb.leitnerbox.core.domain.csv.ParsedCsvCard
import com.jb.leitnerbox.core.domain.usecase.importexport.CsvAnalysisResult
import com.jb.leitnerbox.core.domain.usecase.importexport.CsvImportResult
import com.jb.leitnerbox.core.domain.usecase.importexport.ImportCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val importCsvUseCase: ImportCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvImportUiState())
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    private var importJob: Job? = null

    fun onFileSelected(csvContent: String) {
        if (importJob?.isActive == true) return

        importJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when (val analysis = importCsvUseCase.analyze(csvContent)) {
                    is CsvAnalysisResult.ParseError -> {
                        _uiState.update {
                            it.copy(isLoading = false, parseError = analysis.reason)
                        }
                    }
                    is CsvAnalysisResult.Ready -> {
                        _uiState.update {
                            it.copy(
                                isLoading        = false,
                                pendingCards     = analysis.cards,
                                mergingDeckNames = analysis.mergingDeckNames,
                                mergingCardsCount = analysis.mergingCardsCount,
                                showConfirmDialog = true
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onImportConfirmed() {
        if (importJob?.isActive == true && _uiState.value.isLoading) return
        val cards = _uiState.value.pendingCards ?: return

        importJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            try {
                val result = importCsvUseCase.import(cards)
                _uiState.update {
                    it.copy(
                        isLoading     = false,
                        importResult  = result,
                        pendingCards  = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onImportCancelled() {
        _uiState.update {
            it.copy(showConfirmDialog = false, pendingCards = null, mergingDeckNames = emptyList())
        }
    }

    fun onResultDismissed() {
        _uiState.update { it.copy(importResult = null, parseError = null) }
    }
}

data class CsvImportUiState(
    val isLoading: Boolean = false,
    val pendingCards: List<ParsedCsvCard>? = null,
    val mergingDeckNames: List<String> = emptyList(),
    val mergingCardsCount: Int = 0,
    val showConfirmDialog: Boolean = false,
    val importResult: CsvImportResult? = null,
    val parseError: MalformedReason? = null
)
