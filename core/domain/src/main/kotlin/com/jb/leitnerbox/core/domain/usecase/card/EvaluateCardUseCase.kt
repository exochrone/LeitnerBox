package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.service.SchedulingService
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class EvaluateCardUseCase(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val schedulingService: SchedulingService
) {
    suspend operator fun invoke(
        card: Card, 
        deck: Deck, 
        isCorrect: Boolean,
        referenceInstant: Instant = Instant.now()
    ) {
        val excludedDays = settingsRepository.getExcludedDays().first()
        val today = referenceInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        
        val updatedCard = if (isCorrect) {
            handleCorrectAnswer(card, deck, today, excludedDays)
        } else {
            handleIncorrectAnswer(card, deck, today, excludedDays)
        }

        cardRepository.updateCard(updatedCard.copy(lastReviewDate = referenceInstant))
    }

    private fun handleCorrectAnswer(
        card: Card, 
        deck: Deck, 
        today: LocalDate, 
        excludedDays: Set<java.time.DayOfWeek>
    ): Card {
        val nextBox = card.box + 1
        val isMastered = nextBox > deck.intervals.size

        return if (isMastered) {
            card.copy(
                isLearned = true,
                box = nextBox - 1,
                nextReviewDate = null
            )
        } else {
            val intervalInDays = deck.intervals[nextBox - 1]
            val nextDate = schedulingService.calculateNextReviewDate(today, intervalInDays, excludedDays)
            card.copy(
                box = nextBox,
                nextReviewDate = nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            )
        }
    }

    private fun handleIncorrectAnswer(
        card: Card, 
        deck: Deck, 
        today: LocalDate, 
        excludedDays: Set<java.time.DayOfWeek>
    ): Card {
        val nextBox = if (deck.backToFirstOnFail) {
            1
        } else {
            (card.box - 1).coerceAtLeast(1)
        }

        val intervalInDays = deck.intervals[nextBox - 1]
        val nextDate = schedulingService.calculateNextReviewDate(today, intervalInDays, excludedDays)
        
        return card.copy(
            box = nextBox,
            nextReviewDate = nextDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        )
    }
}