package com.jb.leitnerbox.core.domain.usecase.importexport

import com.jb.leitnerbox.core.domain.csv.*
import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import com.jb.leitnerbox.core.domain.utils.LatexDetector
import kotlinx.coroutines.flow.first
import java.time.Instant

class ImportCsvUseCase(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val settingsRepository: SettingsRepository,
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

        val deckNamesInFile = cards.map { it.deckName.trim() }.distinct()
        val mergingDeckNames = deckNamesInFile.filter { it in existingDeckNames }
        val mergingCardsCount = cards.count { it.deckName.trim() in existingDeckNames }

        return CsvAnalysisResult.Ready(
            cards             = cards,
            mergingDeckNames  = mergingDeckNames,
            mergingCardsCount = mergingCardsCount
        )
    }

    /**
     * Étape 2 : effectuer l'import après confirmation de l'utilisateur.
     */
    suspend fun import(cards: List<ParsedCsvCard>): CsvImportResult {
        val now = Instant.now()
        val nowMs = System.currentTimeMillis()
        val ignoredLines = mutableListOf<Int>()
        val quota = settingsRepository.getNewCardsPerDay().first()

        // 1. Préparer la liste de toutes les cartes à importer (en résolvant les decks et doublons)
        val cardsToInsert = mutableListOf<Card>()

        // On groupe par deck pour créer les decks nécessaires et vérifier les doublons efficacement
        val groupedCards = cards.groupBy { it.deckName.trim() }
        
        for ((deckName, deckCards) in groupedCards) {
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

            val existingNormalized = cardRepository.getCardsByDeckId(deck.id)
                .first()
                .map { it.rectoNormalized }
                .toSet()

            deckCards.forEach { parsed ->
                val normalized = normalizer.normalize(parsed.recto)
                if (normalized in existingNormalized) {
                    ignoredLines.add(parsed.lineNumber)
                } else {
                    val safeNeedsInput = if (LatexDetector.containsLatex(parsed.verso)) false
                                         else parsed.needsInput
                    
                    cardsToInsert.add(
                        Card(
                            deckId           = deck.id,
                            recto            = parsed.recto,
                            verso            = parsed.verso,
                            box              = 1,
                            needsInput       = safeNeedsInput,
                            nextReviewDate   = null, // Sera défini après le shuffle
                            lastReviewDate   = null,
                            rectoNormalized  = normalized,
                            answerNormalized = normalizer.normalize(parsed.verso),
                            isLearned        = false,
                            isActive         = false // Par défaut, on activera après
                        )
                    )
                }
            }
        }

        // 2. Mélanger toutes les nouvelles cartes pour l'activation aléatoire
        val shuffledCards = cardsToInsert.shuffled()
        var inactiveCount = 0
        
        // 3. Appliquer le quota et insérer
        shuffledCards.forEachIndexed { index, card ->
            val isActive = index < quota
            if (!isActive) inactiveCount++
            
            val finalCard = card.copy(
                isActive       = isActive,
                nextReviewDate = if (isActive) now else null,
                importOrder    = nowMs + index
            )
            cardRepository.insertCard(finalCard)
        }

        return CsvImportResult(
            importedCount = shuffledCards.size,
            deckCount     = groupedCards.size,
            inactiveCount = inactiveCount,
            ignoredLines  = ignoredLines
        )
    }
}

sealed class CsvAnalysisResult {
    data class ParseError(val reason: MalformedReason) : CsvAnalysisResult()
    data class Ready(
        val cards: List<ParsedCsvCard>,
        val mergingDeckNames: List<String>,
        val mergingCardsCount: Int
    ) : CsvAnalysisResult()
}

data class CsvImportResult(
    val importedCount: Int,
    val deckCount: Int,
    val inactiveCount: Int,
    val ignoredLines: List<Int>
)
