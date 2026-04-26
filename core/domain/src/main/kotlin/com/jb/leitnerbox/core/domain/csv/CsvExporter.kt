package com.jb.leitnerbox.core.domain.csv

import com.jb.leitnerbox.core.domain.model.Card
import java.time.ZoneId

class CsvExporter {
    fun export(cards: List<Card>): String {
        val sb = StringBuilder()
        sb.append('\uFEFF')  // BOM UTF-8 — compatibilité Excel Windows
        sb.appendLine("question,reponse,saisieRequise,boite,maitrisee,dateDerniereRevision")
        cards.forEach { card ->
            sb.appendLine(buildRow(card))
        }
        return sb.toString()
    }

    private fun buildRow(card: Card): String {
        val lastReview = card.lastReviewDate
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
            ?.toString() ?: ""  // vide si jamais révisée

        return listOf(
            card.recto.escapeCsv(),
            card.verso.escapeCsv(),
            card.needsInput.toString(),
            card.box.toString(),
            card.isLearned.toString(),
            lastReview
        ).joinToString(",")
    }

    private fun String.escapeCsv(): String {
        return if (contains(',') || contains('"') || contains('\n')) {
            "\"${replace("\"", "\"\"")}\""
        } else this
    }
}
