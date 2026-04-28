package com.jb.leitnerbox.core.domain.usecase.settings

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class RescheduleCardsForExcludedDaysUseCase(
    private val cardRepository: CardRepository,
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
     * - Si aucun jour n'est exclu, aucune modification nécessaire
     */
    suspend operator fun invoke() {
        val excludedDays = settingsRepository.getExcludedDays().first()

        if (excludedDays.isEmpty() || excludedDays.size >= 7) return

        val today = LocalDate.now(ZoneId.systemDefault())
        val cards = cardRepository.getAllCards().first()

        cards
            .filter { !it.isLearned && it.nextReviewDate != null }
            .forEach { card ->
                val reviewDate = card.nextReviewDate!!
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                if (!reviewDate.isAfter(today)) return@forEach
                if (reviewDate.dayOfWeek !in excludedDays) return@forEach

                var adjusted = reviewDate
                var safety = 0
                while (adjusted.dayOfWeek in excludedDays && safety < 7) {
                    adjusted = adjusted.plusDays(1)
                    safety++
                }

                if (adjusted.dayOfWeek in excludedDays) return@forEach

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
