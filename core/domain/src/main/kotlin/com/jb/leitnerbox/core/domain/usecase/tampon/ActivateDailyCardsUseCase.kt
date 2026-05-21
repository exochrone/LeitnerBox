package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.model.Card
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
        }

        val activatedToday = if (isNewDay) 0 else settings.cardsActivatedToday
        val maxDaily = settings.maxDailyNewCards

        // 2. Vérification du quota journalier
        if (activatedToday >= maxDaily) return
        val quota = maxDaily - activatedToday

        // 3. Récupération des decks ayant des cartes inactives
        val deckIds = cardRepository.getDeckIdsWithInactiveCards()
        if (deckIds.isEmpty()) return

        val cardsToActivate = mutableListOf<Card>()
        val activeDeckIds = deckIds.toMutableList()
        val selectedCardIds = mutableSetOf<Long>()

        // 4. Boucle d'activation distribuée carte par carte (batchée)
        while (cardsToActivate.size < quota && activeDeckIds.isNotEmpty()) {
            val randomIndex = Random.nextInt(activeDeckIds.size)
            val randomDeckId = activeDeckIds[randomIndex]
            
            // On récupère plusieurs candidats pour éviter les requêtes N+1 si possible,
            // ou on filtre par rapport à ce qu'on a déjà choisi.
            val candidates = cardRepository.getOldestInactiveCards(randomDeckId, quota)
                .filter { it.id !in selectedCardIds }

            if (candidates.isNotEmpty()) {
                val targetCard = candidates.first()
                selectedCardIds.add(targetCard.id)
                cardsToActivate.add(
                    targetCard.copy(
                        isActive = true,
                        nextReviewDate = Instant.now()
                    )
                )
            } else {
                // Ce deck n'a plus de cartes inactives non sélectionnées
                activeDeckIds.removeAt(randomIndex)
            }
        }

        // 5. Exécution des mises à jour en batch
        if (cardsToActivate.isNotEmpty()) {
            cardRepository.updateCards(cardsToActivate)
            settingsRepository.updateCardsActivatedToday(activatedToday + cardsToActivate.size)
        }
    }
}
