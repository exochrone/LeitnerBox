package com.jb.leitnerbox.core.domain.usecase.backup

import com.jb.leitnerbox.core.domain.backup.*
import com.jb.leitnerbox.core.domain.repository.*
import kotlinx.coroutines.flow.first
import java.time.Instant

class ExportBackupUseCase(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val serializer: BackupSerializer,
    private val appVersionName: String
) {
    suspend operator fun invoke(): ByteArray {
        val decks = deckRepository.getDecks().first()
        val sessions = sessionRepository.getSessions().first()
        val excludedDays = settingsRepository.getExcludedDays().first()
        val theme = settingsRepository.getTheme().first()
        val notifTime = settingsRepository.getNotificationTime().first()

        val deckBackups = decks.map { deck ->
            val cards = cardRepository.getCardsByDeckId(deck.id).first()
            DeckBackup(
                id = deck.id,
                name = deck.name,
                description = deck.description,
                intervals = deck.intervals,
                wrongAnswerRule = deck.wrongAnswerRule.name,
                presentationOrder = deck.presentationOrder.name,
                color = deck.color,
                cards = cards.map { card ->
                    CardBackup(
                        id = card.id,
                        recto = card.recto,
                        verso = card.verso,
                        box = card.box,
                        isLearned = card.isLearned,
                        needsInput = card.needsInput,
                        nextReviewDate = card.nextReviewDate?.toString(),
                        lastReviewDate = card.lastReviewDate?.toString(),
                        rectoNormalized = card.rectoNormalized,
                        answerNormalized = card.answerNormalized
                    )
                }
            )
        }

        val data = BackupData(
            exportDate = Instant.now().toString(),
            appVersion = appVersionName,
            decks = deckBackups,
            sessions = sessions.map { session ->
                SessionBackup(
                    id = session.id,
                    date = session.date.toString(),
                    deckIds = session.deckIds,
                    cardCount = session.cardCount,
                    successCount = session.successCount,
                    masteredCount = session.masteredCount,
                    advancedCount = session.advancedCount,
                    retreatedCount = session.retreatedCount,
                    isReported = session.isReported
                )
            },
            settings = SettingsBackup(
                excludedDays = excludedDays.map { it.value },
                theme = theme.name,
                notificationHour = notifTime.hour,
                notificationMinute = notifTime.minute
            )
        )

        return serializer.serialize(data)
    }
}
