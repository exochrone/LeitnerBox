package com.jb.leitnerbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.leitnerbox.core.domain.csv.MalformedReason
import com.jb.leitnerbox.core.domain.csv.ParsedCsvCard
import com.jb.leitnerbox.core.domain.usecase.importexport.CsvAnalysisResult
import com.jb.leitnerbox.core.domain.usecase.importexport.CsvImportResult
import com.jb.leitnerbox.core.domain.usecase.importexport.ImportCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val importCsvUseCase: ImportCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvImportUiState())
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    fun onFileSelected(csvContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
                            showConfirmDialog = true
                        )
                    }
                }
            }
        }
    }

    fun onImportConfirmed() {
        val cards = _uiState.value.pendingCards ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmDialog = false) }
            val result = importCsvUseCase.import(cards)
            _uiState.update {
                it.copy(
                    isLoading     = false,
                    importResult  = result,
                    pendingCards  = null
                )
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
    val showConfirmDialog: Boolean = false,
    val importResult: CsvImportResult? = null,
    val parseError: MalformedReason? = null
)
