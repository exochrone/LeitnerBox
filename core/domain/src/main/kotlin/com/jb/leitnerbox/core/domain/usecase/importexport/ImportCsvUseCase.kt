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
import java.time.temporal.ChronoUnit

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
        
        // 1. Extraction unique des configurations globales (Évite les appels répétés en boucle)
        val maxQuota = settingsRepository.getNewCardsPerDay().first()
        val allCardsInDb = cardRepository.getAllCards().first()
        
        // Calcul du tampon restant pour aujourd'hui (cartes actives sans historique ou révisées aujourd'hui)
        val startOfToday = now.truncatedTo(ChronoUnit.DAYS)
        val cardsActivatedToday = allCardsInDb.count { card ->
            card.isActive && (card.lastReviewDate == null || card.lastReviewDate.isAfter(startOfToday))
        }
        val remainingQuota = (maxQuota - cardsActivatedToday).coerceAtLeast(0)

        // Indexation des ressources existantes pour accélération O(1)
        var currentDecksMap = deckRepository.getDecks().first().associateBy { it.name }
        val allCardsByDeck = allCardsInDb.groupBy { it.deckId }
        
        val cardsToInsert = mutableListOf<Card>()
        val groupedCards = cards.groupBy { it.deckName.trim() }
        
        for ((deckName, deckCards) in groupedCards) {
            var deck = currentDecksMap[deckName]

            if (deck == null) {
                val newDeck = Deck(
                    name              = deckName,
                    intervals         = listOf(1, 3, 5, 7, 14),
                    wrongAnswerRule   = WrongAnswerRule.BACK_TO_BOX_ONE,
                    presentationOrder = PresentationOrder.RANDOM,
                    color             = "default"
                )
                val id = deckRepository.insertDeck(newDeck)
                deck = newDeck.copy(id = id)
                // Mettre à jour la map locale
                currentDecksMap = currentDecksMap + (deckName to deck)
            }

            // Récupération optimisée des doublons via la map en mémoire
            val existingNormalized = allCardsByDeck[deck.id]
                ?.map { it.rectoNormalized }
                ?.toSet() ?: emptySet()
            
            val seenInThisImport = mutableSetOf<String>()

            deckCards.forEach { parsed ->
                val normalized = normalizer.normalize(parsed.recto)
                if (normalized in existingNormalized || normalized in seenInThisImport) {
                    ignoredLines.add(parsed.lineNumber)
                } else {
                    seenInThisImport.add(normalized)
                    val safeNeedsInput = if (LatexDetector.containsLatex(parsed.verso)) false 
                                         else parsed.needsInput
                    
                    cardsToInsert.add(
                        Card(
                            deckId           = deck.id,
                            recto            = parsed.recto,
                            verso            = parsed.verso,
                            box              = 1,
                            needsInput       = safeNeedsInput,
                            nextReviewDate   = null,
                            lastReviewDate   = null,
                            rectoNormalized  = normalized,
                            answerNormalized = normalizer.normalize(parsed.verso),
                            isLearned        = false,
                            isActive         = false
                        )
                    )
                }
            }
        }

        // 2. Mélange et activation selon le quota restant
        val shuffledCards = cardsToInsert.shuffled()
        var inactiveCount = 0
        
        shuffledCards.forEachIndexed { index, card ->
            val isActive = index < remainingQuota
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
