package com.jb.leitnerbox.feature.importexport.ui

import com.jb.leitnerbox.core.domain.csv.DuplicateEntry

data class ImportExportUiState(
    val targetDeckId: Long,
    val targetDeckName: String = "",
    val exportCardCount: Int = 0,
    val isLoading: Boolean = false,
    val result: ImportUiResult? = null
)

sealed class ImportUiResult {
    data class Success(val count: Int) : ImportUiResult()
    data class DuplicatesFound(val entries: List<DuplicateEntry>) : ImportUiResult()
    object MalformedFile : ImportUiResult()
    object EncodingError : ImportUiResult()
    object EmptyFile : ImportUiResult()
    object MissingColumns : ImportUiResult()
}
