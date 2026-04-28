package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class RescheduleCardsForExcludedDaysUseCase(
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Recalcule les nextReviewDate futures qui tombent sur un jour exclu.
     * Lit l'état actuel des jours exclus directement depuis le DataStore.
     *
     * Règles :
     * - Seules les dates strictement futures sont modifiées
     * - Les cartes maîtrisées (isLearned = true) sont ignorées
     * - Si tous les jours sont exclus, aucune modification (protection anti-boucle)
     * - Si aucun jour n'est exclu, le calcul repart de lastReviewDate + intervalle
     */
    suspend operator fun invoke() {
        val excludedDays = settingsRepository.getExcludedDays().first()
        val today = LocalDate.now(ZoneId.systemDefault())
        val cards = cardRepository.getAllCards().first()
        val decks = deckRepository.getDecks().first()

        cards
            .filter { !it.isLearned && it.lastReviewDate != null }
            .forEach { card ->
                val deck = decks.firstOrNull { it.id == card.deckId } ?: return@forEach
                val boxIndex = (card.box - 1).coerceIn(0, deck.intervals.size - 1)
                val interval = deck.intervals[boxIndex].toLong()

                // Date naturelle = dernière révision + intervalle de la boîte
                val naturalDate = card.lastReviewDate!!
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .plusDays(interval)

                // Ne modifier que les dates futures
                if (!naturalDate.isAfter(today)) return@forEach

                // Sauter les jours exclus
                var adjusted = naturalDate
                var safety = 0
                while (adjusted.dayOfWeek in excludedDays && safety < 7) {
                    adjusted = adjusted.plusDays(1)
                    safety++
                }
                if (adjusted.dayOfWeek in excludedDays) return@forEach

                // Mettre à jour uniquement si la date change
                val currentDate = card.nextReviewDate
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
                if (adjusted != currentDate) {
                    cardRepository.updateCard(
                        card.copy(
                            nextReviewDate = adjusted
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                        )
                    )
                }
            }
    }
}
