package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.first

class BuildExtraSessionUseCase(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(deckId: Long): List<Card> =
        cardRepository.getCardsByDeckId(deckId)
            .first()
            .filter { it.isActive && !it.isLearned }
            .shuffled()
}
