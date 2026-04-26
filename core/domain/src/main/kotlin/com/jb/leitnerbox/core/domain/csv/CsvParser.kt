package com.jb.leitnerbox.core.domain.csv

class CsvParser {
    fun parse(csvContent: String): CsvParseResult {
        // Trim du BOM UTF-8 si présent (compatibilité fichiers Excel)
        val content = csvContent.trimStart('\uFEFF')

        if (content.isBlank()) {
            return CsvParseResult.Error(MalformedReason.EMPTY_FILE)
        }

        val lines = content.lines()
            .map { it.trimEnd('\r') }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return CsvParseResult.Error(MalformedReason.EMPTY_FILE)

        // Parser les en-têtes
        val headers = parseRow(lines.first()).map { it.trim().lowercase() }
        val questionIdx = headers.indexOf("question")
        val reponseIdx = headers.indexOf("reponse")
        val saisieIdx = headers.indexOf("saisierequise")

        if (questionIdx == -1 || reponseIdx == -1) {
            return CsvParseResult.Error(MalformedReason.MISSING_REQUIRED_COLUMN)
        }

        // Parser les lignes de données
        val cards = mutableListOf<ParsedCard>()
        lines.drop(1).forEachIndexed { index, line ->
            val lineNumber = index + 2  // +2 car on commence à la ligne 2 (après en-têtes)
            val fields = parseRow(line)

            if (fields.size <= maxOf(questionIdx, reponseIdx)) {
                return CsvParseResult.Error(MalformedReason.INVALID_FORMAT)
            }

            val recto = fields[questionIdx].trim()
            val verso = fields[reponseIdx].trim()
            val needsInput = saisieIdx != -1 &&
                fields.getOrNull(saisieIdx)?.trim()?.lowercase() == "true"

            cards.add(ParsedCard(lineNumber, recto, verso, needsInput))
        }

        return CsvParseResult.Success(cards)
    }

    private fun parseRow(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++  // sauter le doublon
                }
                c == '"' && inQuotes -> inQuotes = false
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}

sealed class CsvParseResult {
    data class Success(val cards: List<ParsedCard>) : CsvParseResult()
    data class Error(val reason: MalformedReason) : CsvParseResult()
}
