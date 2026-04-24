package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant

class CancelPostponeBoxUseCase(
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(deckId: Long, boxNumber: Int, sessionId: Long) {
        // 1. Remettre les cartes à aujourd'hui (ou Instant.MIN pour être sûr qu'elles apparaissent)
        val cards = cardRepository.getCardsByDeckId(deckId).first()
        val cardsToReset = cards.filter { it.box == boxNumber && !it.isLearned }
        
        cardsToReset.forEach { card ->
            cardRepository.updateCard(card.copy(nextReviewDate = Instant.now()))
        }

        // 2. Supprimer la session de report associée
        val sessions = sessionRepository.getSessions().first()
        sessions.find { it.id == sessionId }?.let {
            sessionRepository.deleteSession(it)
        }
    }
}
