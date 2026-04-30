package com.jb.leitnerbox.core.domain.csv

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck

class CsvExporter {
    /**
     * Exporte une map de decks et leurs cartes vers une chaîne CSV.
     * Séparateur : ;
     * Encodage : UTF-8 avec BOM
     */
    fun export(deckCardsMap: Map<Deck, List<Card>>): String {
        val sb = StringBuilder()
        sb.append('\uFEFF')  // BOM UTF-8
        sb.appendLine("nomDeck;question;reponse;saisieRequise")

        deckCardsMap.forEach { (deck, cards) ->
            cards.forEach { card ->
                sb.appendLine(buildRow(deck.name, card))
            }
        }
        return sb.toString()
    }

    private fun buildRow(deckName: String, card: Card): String {
        return listOf(
            deckName.escapeCsvSemicolon(),
            card.recto.escapeCsvQuoted(),
            card.verso.escapeCsvQuoted(),
            if (card.needsInput) "oui" else "non"
        ).joinToString(";")
    }

    // Champs nomDeck : guillemets uniquement si contient ";"
    private fun String.escapeCsvSemicolon(): String =
        if (contains(';') || contains('"')) "\"${replace("\"", "\"\"")}\"" else this

    // Champs question/réponse : toujours entre guillemets
    private fun String.escapeCsvQuoted(): String =
        "\"${replace("\"", "\"\"")}\""
}
