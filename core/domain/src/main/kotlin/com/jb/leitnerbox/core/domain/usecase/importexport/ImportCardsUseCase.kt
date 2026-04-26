package com.jb.leitnerbox.core.domain.usecase.importexport

import com.jb.leitnerbox.core.domain.csv.*
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlinx.coroutines.flow.first
import java.time.Instant

class ImportCardsUseCase(
    private val cardRepository: CardRepository,
    private val parser: CsvParser,
    private val normalizer: AnswerNormalizer
) {
    suspend operator fun invoke(
        csvContent: String,
        deckId: Long
    ): ImportResult {
        // 1. Parser le fichier
        val parseResult = parser.parse(csvContent)
        if (parseResult is CsvParseResult.Error) {
            return ImportResult.MalformedFile(parseResult.reason)
        }
        val parsedCards = (parseResult as CsvParseResult.Success).cards

        // 2. Détecter les doublons internes au fichier
        val duplicates = mutableListOf<DuplicateEntry>()
        val seenInFile = mutableMapOf<String, Int>()  // normalizedRecto → lineNumber

        parsedCards.forEach { card ->
            val normalized = normalizer.normalize(card.recto)
            val existingLine = seenInFile[normalized]
            if (existingLine != null) {
                duplicates.add(DuplicateEntry(card.lineNumber, card.recto, DuplicateSource.WITHIN_FILE))
            } else {
                seenInFile[normalized] = card.lineNumber
            }
        }

        // 3. Détecter les doublons avec le deck existant
        val existingCards = cardRepository.getCardsByDeckId(deckId).first()
        val existingNormalized = existingCards.map { it.rectoNormalized }.toSet()

        parsedCards.forEach { card ->
            val normalized = normalizer.normalize(card.recto)
            if (normalized in existingNormalized && !duplicates.any { it.lineNumber == card.lineNumber }) {
                duplicates.add(DuplicateEntry(card.lineNumber, card.recto, DuplicateSource.WITH_DECK))
            }
        }

        // 4. Bloquer si doublons
        if (duplicates.isNotEmpty()) {
            return ImportResult.DuplicatesFound(duplicates)
        }

        // 5. Importer les cartes (écriture en base uniquement si pas de doublon)
        val now = Instant.now()
        parsedCards.forEach { parsed ->
            cardRepository.insertCard(
                Card(
                    deckId = deckId,
                    recto = parsed.recto,
                    verso = parsed.verso,
                    needsInput = parsed.needsInput,
                    box = 1,
                    nextReviewDate = now,
                    rectoNormalized = normalizer.normalize(parsed.recto),
                    answerNormalized = normalizer.normalize(parsed.verso),
                    isLearned = false
                )
            )
        }

        return ImportResult.Success(parsedCards.size)
    }
}
