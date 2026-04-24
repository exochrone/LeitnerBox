package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.SessionPlanItem
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.first

class BuildSessionUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(
        selectedItems: List<SessionPlanItem>
    ): List<Card> {
        return selectedItems
            .flatMap { item ->
                cardRepository.getCardsByDeckId(item.deck.id)
                    .first()
                    .filter { card -> card.box == item.boxNumber && !card.isLearned }
            }
            .shuffled()
    }
}
