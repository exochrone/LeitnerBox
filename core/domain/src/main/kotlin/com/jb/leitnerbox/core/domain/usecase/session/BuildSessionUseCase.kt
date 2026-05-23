package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.SessionPlanItem
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class BuildSessionUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(
        selectedItems: List<SessionPlanItem>
    ): List<Card> {
        val today = LocalDate.now(ZoneId.systemDefault())

        return selectedItems
            .flatMap { item ->
                cardRepository.getCardsByDeckId(item.deck.id)
                    .first()
                    .filter { card ->
                        if (card.box != item.boxNumber || card.isLearned || !card.isActive) return@filter false

                        val nextReview = card.nextReviewDate?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        nextReview != null && (nextReview.isBefore(today) || nextReview.isEqual(today))
                    }
            }
            .shuffled()
    }
}
