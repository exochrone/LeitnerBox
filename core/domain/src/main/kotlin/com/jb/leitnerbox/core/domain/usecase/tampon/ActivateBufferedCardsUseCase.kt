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
    suspend operator fun invoke() {
        // 1. Récupérer la taille limite du tampon global (depuis le Proto DataStore)
        val globalBufferSize = settingsRepository.getBufferSize().first()
        
        // 2. Récupérer le total actuel de cartes actives en Boîte 1
        val currentGlobalActiveCount = cardRepository.countGlobalActiveCardsInBoxOne().first()
        
        // 3. Sortie précoce si le plafond global est atteint ou dépassé
        if (currentGlobalActiveCount >= globalBufferSize) return
        
        // 4. Calcul du quota disponible
        val quota = globalBufferSize - currentGlobalActiveCount
        
        // 5. Extraction chronologique inter-decks
        val inactiveCards = cardRepository.getGlobalOldestInactiveCards(quota)
        if (inactiveCards.isEmpty()) return

        // 6. Activation et normalisation à la volée
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
