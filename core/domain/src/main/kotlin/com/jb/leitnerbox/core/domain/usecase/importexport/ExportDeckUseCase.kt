package com.jb.leitnerbox.core.domain.usecase.importexport

import com.jb.leitnerbox.core.domain.csv.CsvExporter
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.first

class ExportDeckUseCase(
    private val cardRepository: CardRepository,
    private val exporter: CsvExporter
) {
    suspend operator fun invoke(deckId: Long): String {
        val cards = cardRepository.getCardsByDeckId(deckId).first()
        return exporter.export(cards)
    }
}
