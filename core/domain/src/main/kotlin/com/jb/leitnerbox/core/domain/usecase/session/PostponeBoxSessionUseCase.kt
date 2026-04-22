package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.service.SchedulingService
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId

class PostponeBoxSessionUseCase(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val schedulingService: SchedulingService
) {
    suspend operator fun invoke(deckId: Long, boxNumber: Int) {
        val deck = deckRepository.getDeckById(deckId).first() ?: return
        val cards = cardRepository.getCardsByDeckId(deckId).first()
        
        val now = Instant.now()
        val today = now.atZone(ZoneId.systemDefault()).toLocalDate()
        
        // On calcule la date du "prochain jour travaillé"
        val nextAvailableDate = schedulingService.calculateNextReviewDate(
            fromDate = today,
            intervalInDays = 1,
            excludedDays = deck.excludedDays
        )
        
        val nextAvailableInstant = nextAvailableDate.atStartOfDay(ZoneId.systemDefault()).toInstant()

        val cardsToPostpone = cards.filter { card ->
            val nextReview = card.nextReviewDate
            card.box == boxNumber && (nextReview == null || nextReview <= now)
        }

        cardsToPostpone.forEach { card ->
            cardRepository.updateCard(card.copy(nextReviewDate = nextAvailableInstant))
        }
    }
}
