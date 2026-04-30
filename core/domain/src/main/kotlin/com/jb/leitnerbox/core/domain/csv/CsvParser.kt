package com.jb.leitnerbox.core.domain.csv

class CsvParser {

    companion object {
        val EXPECTED_HEADERS = listOf("nomdeck", "question", "reponse", "saisierequise")
    }

    fun parse(csvContent: String): CsvParseResult {
        // Trim du BOM UTF-8 si présent (compatibilité fichiers Excel)
        val content = csvContent.trimStart('\uFEFF')

        if (content.isBlank()) {
            return CsvParseResult.Error(MalformedReason.EMPTY_FILE)
        }

        val lines = splitRespectingQuotes(content)
        if (lines.isEmpty()) return CsvParseResult.Error(MalformedReason.EMPTY_FILE)

        // Validation des en-têtes
        val headers = parseRow(lines.first(), ';').map { it.trim().lowercase() }
        if (headers != EXPECTED_HEADERS) {
            return CsvParseResult.Error(MalformedReason.MISSING_REQUIRED_COLUMN)
        }

        // Parsing des données
        val cards = mutableListOf<ParsedCsvCard>()
        lines.drop(1).forEachIndexed { index, line ->
            val lineNumber = index + 2
            if (line.isBlank()) return@forEachIndexed
            val fields = parseRow(line, ';')
            if (fields.size < 4) return CsvParseResult.Error(MalformedReason.INVALID_FORMAT)

            cards.add(
                ParsedCsvCard(
                    lineNumber  = lineNumber,
                    deckName    = fields[0].trim(),
                    recto       = fields[1],
                    verso       = fields[2],
                    needsInput  = fields[3].trim().lowercase() == "oui"
                )
            )
        }

        return CsvParseResult.Success(cards)
    }

    /**
     * Découpe le contenu en lignes en respectant les sauts de ligne
     * à l'intérieur des champs entre guillemets.
     */
    private fun splitRespectingQuotes(content: String): List<String> {
        val lines = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < content.length) {
            val c = content[i]
            when {
                c == '"' -> {
                    inQuotes = !inQuotes
                    current.append(c)
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < content.length && content[i + 1] == '\n') i++
                    lines.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    /**
     * Parser RFC conforme pour un séparateur configurable.
     */
    private fun parseRow(line: String, separator: Char): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"'); i++
                }
                c == '"' && inQuotes -> inQuotes = false
                c == separator && !inQuotes -> {
                    fields.add(current.toString()); current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}

data class ParsedCsvCard(
    val lineNumber: Int,
    val deckName: String,
    val recto: String,
    val verso: String,
    val needsInput: Boolean
)

sealed class CsvParseResult {
    data class Success(val cards: List<ParsedCsvCard>) : CsvParseResult()
    data class Error(val reason: MalformedReason) : CsvParseResult()
}

enum class MalformedReason {
    MISSING_REQUIRED_COLUMN,
    EMPTY_FILE,
    INVALID_FORMAT
}
