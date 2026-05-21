package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class ActivateDailyCardsUseCase(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val today = LocalDate.now()
        val settings = settingsRepository.settings.first()
        
        // 1. Gestion du changement de jour (Reset)
        val lastActivationDate = settings.lastActivationDateIso?.let { 
            runCatching { LocalDate.parse(it) }.getOrNull() 
        }
        
        val isNewDay = lastActivationDate == null || today.isAfter(lastActivationDate)
        if (isNewDay) {
            settingsRepository.updateLastActivationDate(today.toString())
            // On ne reset pas immédiatement en base, on le fera à la fin avec la valeur finale
        }

        var activatedToday = if (isNewDay) 0 else settings.cardsActivatedToday
        val maxDaily = settings.maxDailyNewCards

        // 2. Vérification du quota journalier
        if (activatedToday >= maxDaily) return
        var quotaRemaining = maxDaily - activatedToday

        // 3. Boucle d'activation distribuée carte par carte
        while (quotaRemaining > 0) {
            val deckIds = cardRepository.getDeckIdsWithInactiveCards()
            if (deckIds.isEmpty()) break // Plus aucune carte inactive dans l'application

            // Sélection aléatoire du deck (RG 2.4)
            val randomDeckId = deckIds[Random.nextInt(deckIds.size)]
            val targetCard = cardRepository.getOldestInactiveCardForDeck(randomDeckId)

            if (targetCard != null) {
                // Activation de la carte cible
                cardRepository.updateCard(
                    targetCard.copy(
                        isActive = true,
                        nextReviewDate = Instant.now()
                    )
                )
                activatedToday++
                quotaRemaining--
            }
        }

        // 4. Persister le nouveau compteur
        settingsRepository.updateCardsActivatedToday(activatedToday)
    }
}
