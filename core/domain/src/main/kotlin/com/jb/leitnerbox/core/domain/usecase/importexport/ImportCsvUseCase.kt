package com.jb.leitnerbox.core.domain.usecase.importexport

import com.jb.leitnerbox.core.domain.csv.*
import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import com.jb.leitnerbox.core.domain.utils.LatexDetector
import kotlinx.coroutines.flow.first
import java.time.Instant

class ImportCsvUseCase(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val parser: CsvParser,
    private val normalizer: AnswerNormalizer
) {
    /**
     * Étape 1 : analyser le fichier et retourner les infos avant import.
     * Aucune écriture en base à ce stade.
     */
    suspend fun analyze(csvContent: String): CsvAnalysisResult {
        val parseResult = parser.parse(csvContent)
        if (parseResult is CsvParseResult.Error) {
            return CsvAnalysisResult.ParseError(parseResult.reason)
        }
        val cards = (parseResult as CsvParseResult.Success).cards

        val existingDecks = deckRepository.getDecks().first()
        val existingDeckNames = existingDecks.map { it.name }.toSet()

        val deckNamesInFile = cards.map { it.deckName }.distinct()
        val mergingDeckNames = deckNamesInFile.filter { it in existingDeckNames }

        return CsvAnalysisResult.Ready(
            cards            = cards,
            mergingDeckNames = mergingDeckNames
        )
    }

    /**
     * Étape 2 : effectuer l'import après confirmation de l'utilisateur.
     */
    suspend fun import(cards: List<ParsedCsvCard>): CsvImportResult {
        val now = Instant.now()
        val ignoredLines = mutableListOf<Int>()
        var importedCount = 0

        // Grouper par deck pour minimiser les appels repository
        cards.groupBy { it.deckName }.forEach { (deckName, deckCards) ->
            // Re-fetch les decks à chaque itération car on peut en créer
            val currentDecks = deckRepository.getDecks().first()
            val existingDeck = currentDecks.firstOrNull { it.name == deckName }

            val deck = existingDeck ?: run {
                val newDeck = Deck(
                    name              = deckName,
                    intervals         = listOf(1, 3, 5, 7, 14),
                    wrongAnswerRule   = WrongAnswerRule.BACK_TO_BOX_ONE,
                    presentationOrder = PresentationOrder.RANDOM,
                    color             = "default"
                )
                val id = deckRepository.insertDeck(newDeck)
                newDeck.copy(id = id)
            }

            // Récupérer les questions existantes dans ce deck
            val existingNormalized = cardRepository.getCardsByDeckId(deck.id)
                .first()
                .map { it.rectoNormalized }
                .toSet()

            deckCards.forEach { parsed ->
                val normalized = normalizer.normalize(parsed.recto)
                if (normalized in existingNormalized) {
                    ignoredLines.add(parsed.lineNumber)
                    return@forEach
                }

                // Forcer needsInput = false si verso contient du LaTeX
                val safeNeedsInput = if (LatexDetector.containsLatex(parsed.verso)) false
                                     else parsed.needsInput

                cardRepository.insertCard(
                    Card(
                        deckId           = deck.id,
                        recto            = parsed.recto,
                        verso            = parsed.verso,
                        box              = 1,
                        needsInput       = safeNeedsInput,
                        nextReviewDate   = now,
                        lastReviewDate   = null,
                        rectoNormalized  = normalized,
                        answerNormalized = normalizer.normalize(parsed.verso),
                        isLearned        = false
                    )
                )
                importedCount++
            }
        }

        return CsvImportResult(
            importedCount = importedCount,
            ignoredLines  = ignoredLines
        )
    }
}

sealed class CsvAnalysisResult {
    data class ParseError(val reason: MalformedReason) : CsvAnalysisResult()
    data class Ready(
        val cards: List<ParsedCsvCard>,
        val mergingDeckNames: List<String>
    ) : CsvAnalysisResult()
}

data class CsvImportResult(
    val importedCount: Int,
    val ignoredLines: List<Int>
)
