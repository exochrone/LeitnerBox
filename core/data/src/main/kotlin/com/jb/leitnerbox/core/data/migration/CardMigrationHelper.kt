package com.jb.leitnerbox.core.data.migration

import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlinx.coroutines.flow.first

class CardMigrationHelper(
    private val cardDao: CardDao,
    private val answerNormalizer: AnswerNormalizer
) {
    /**
     * Recalcule rectoNormalized et answerNormalized pour toutes les cartes
     * dont ces champs sont vides (cartes migrées depuis la version 1).
     * Cette méthode est idempotente : elle peut être appelée plusieurs fois
     * sans effet de bord.
     */
    suspend fun repopulateNormalizedFields() {
        cardDao.getAllCards()
            .first()
            .filter { it.rectoNormalized.isEmpty() || it.answerNormalized.isEmpty() }
            .forEach { entity ->
                val updated = entity.copy(
                    rectoNormalized  = answerNormalizer.normalize(entity.recto),
                    answerNormalized = answerNormalizer.normalize(entity.verso)
                )
                cardDao.updateCard(updated)
            }
    }
}
