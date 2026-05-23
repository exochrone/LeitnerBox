package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.Instant
import kotlinx.coroutines.flow.first

class ActivateDailyCardsUseCase(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        val today = LocalDate.now()
        val settings = settingsRepository.settings.first()

        // Remise à zéro si nouveau jour
        val lastDate = settings.lastActivationDateIso
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val activatedToday = if (lastDate == null || today.isAfter(lastDate)) {
            settingsRepository.updateLastActivationDate(today.toString())
            settingsRepository.updateCardsActivatedToday(0)
            0
        } else {
            settings.cardsActivatedToday
        }

        // Quota atteint : rien à faire
        val quota = settings.maxDailyNewCards - activatedToday
        if (quota <= 0) return

        // Récupérer les decks ayant des cartes inactives
        val deckIds = cardRepository.getDeckIdsWithInactiveCards()
        if (deckIds.isEmpty()) return

        // Rotation round-robin entre les decks
        // Pour chaque deck, on pré-charge ses cartes inactives dans une file FIFO
        val queues: MutableMap<Long, ArrayDeque<Card>> = deckIds.associateWith { deckId ->
            ArrayDeque(cardRepository.getOldestInactiveCards(deckId, quota))
        }.filter { it.value.isNotEmpty() }.toMutableMap()

        val activeDeckIds = queues.keys.toMutableList()
        val cardsToActivate = mutableListOf<Card>()
        var rotationIndex = 0

        while (cardsToActivate.size < quota && activeDeckIds.isNotEmpty()) {
            val deckId = activeDeckIds[rotationIndex % activeDeckIds.size]
            val queue = queues[deckId]!!

            val card = queue.removeFirstOrNull()
            if (card != null) {
                cardsToActivate.add(card.copy(isActive = true, nextReviewDate = Instant.now()))
                rotationIndex++
            } else {
                // Ce deck est épuisé, on le retire de la rotation
                activeDeckIds.remove(deckId)
                // Ne pas incrémenter rotationIndex pour ne pas sauter le deck suivant
            }
        }

        if (cardsToActivate.isEmpty()) return

        // Activer en batch
        cardRepository.updateCards(cardsToActivate)
        settingsRepository.updateCardsActivatedToday(activatedToday + cardsToActivate.size)
    }
}
