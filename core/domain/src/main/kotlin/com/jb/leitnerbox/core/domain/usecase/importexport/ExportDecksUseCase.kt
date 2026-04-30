package com.jb.leitnerbox.core.domain.usecase.importexport

import com.jb.leitnerbox.core.domain.csv.CsvExporter
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.first

class ExportDecksUseCase(
    private val cardRepository: CardRepository,
    private val exporter: CsvExporter
) {
    suspend operator fun invoke(decks: List<Deck>): ExportResult {
        val deckCardsMap = decks.associateWith { deck ->
            cardRepository.getCardsByDeckId(deck.id).first()
        }.filter { (_, cards) -> cards.isNotEmpty() }

        val totalCards = deckCardsMap.values.sumOf { it.size }
        val csvContent = exporter.export(deckCardsMap)

        return ExportResult(
            csvContent  = csvContent,
            deckCount   = deckCardsMap.size,
            cardCount   = totalCards
        )
    }
}

data class ExportResult(
    val csvContent: String,
    val deckCount: Int,
    val cardCount: Int
)
