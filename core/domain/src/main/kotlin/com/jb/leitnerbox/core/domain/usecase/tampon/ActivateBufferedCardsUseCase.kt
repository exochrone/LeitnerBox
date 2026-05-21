package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlinx.coroutines.flow.first
import java.time.Instant

class ActivateBufferedCardsUseCase(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val normalizer: AnswerNormalizer
) {
    suspend operator fun invoke(deckId: Long) {
        val bufferSize = settingsRepository.getBufferSize().first() // ex: 20
        val activeCount = cardRepository.countActiveCardsInBoxOne(deckId).first()
        
        if (activeCount >= bufferSize) return
        
        val quota = bufferSize - activeCount
        val inactiveCards = cardRepository.getOldestInactiveCards(deckId, quota)
        
        if (inactiveCards.isEmpty()) return

        val now = Instant.now()
        val activatedCards = inactiveCards.map { card ->
            card.copy(
                isActive = true,
                nextReviewDate = now,
                rectoNormalized = normalizer.normalize(card.recto),
                answerNormalized = normalizer.normalize(card.verso)
            )
        }
        cardRepository.updateCards(activatedCards)
    }
}
