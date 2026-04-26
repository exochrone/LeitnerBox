package com.jb.leitnerbox.core.domain.csv

sealed class ImportResult {
    data class Success(val importedCount: Int) : ImportResult()
    data class DuplicatesFound(val duplicates: List<DuplicateEntry>) : ImportResult()
    data class MalformedFile(val reason: MalformedReason) : ImportResult()
}

data class DuplicateEntry(
    val lineNumber: Int,
    val question: String,
    val source: DuplicateSource
)

enum class DuplicateSource {
    WITHIN_FILE,
    WITH_DECK
}

enum class MalformedReason {
    MISSING_REQUIRED_COLUMN,
    ENCODING_ERROR,
    EMPTY_FILE,
    INVALID_FORMAT
}
