package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.Instant
import kotlinx.coroutines.flow.first

class ActivateInactiveCardsUseCase(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * Active les N premières cartes inactives d'un deck (ordre FIFO).
     * Si count est null, utilise newCardsPerDay.
     */
    suspend operator fun invoke(
        deckId: Long,
        count: Int? = null
    ): Int {  // retourne le nombre de cartes effectivement activées
        val quota = count ?: settingsRepository.getNewCardsPerDay().first()
        val inactiveCards = cardRepository.getInactiveCards(deckId, limit = quota)
        val now = Instant.now()

        inactiveCards.forEach { card ->
            cardRepository.updateCard(
                card.copy(
                    isActive = true,
                    nextReviewDate = now
                )
            )
        }
        return inactiveCards.size
    }
}
