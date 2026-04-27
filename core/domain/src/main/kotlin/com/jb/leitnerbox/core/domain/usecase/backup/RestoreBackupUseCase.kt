package com.jb.leitnerbox.core.domain.usecase.backup

import com.jb.leitnerbox.core.domain.backup.*
import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.repository.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

class RestoreBackupUseCase(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val serializer: BackupSerializer
) {
    suspend fun validate(bytes: ByteArray): BackupSummary {
        val data = serializer.deserialize(bytes)
        if (data.version > BACKUP_VERSION) {
            throw BackupException.IncompatibleVersion(data.version, BACKUP_VERSION)
        }
        return BackupSummary(
            exportDate = data.exportDate,
            deckCount = data.decks.size,
            cardCount = data.decks.sumOf { it.cards.size },
            sessionCount = data.sessions.size
        )
    }

    suspend fun restore(bytes: ByteArray) {
        val data = serializer.deserialize(bytes)

        deckRepository.deleteAllDecks()
        sessionRepository.deleteAllSessions()

        data.decks.forEach { deckBackup ->
            val deckId = deckRepository.insertDeckWithId(
                Deck(
                    id = deckBackup.id,
                    name = deckBackup.name,
                    description = deckBackup.description,
                    intervals = deckBackup.intervals,
                    wrongAnswerRule = WrongAnswerRule.valueOf(deckBackup.wrongAnswerRule),
                    presentationOrder = PresentationOrder.valueOf(deckBackup.presentationOrder),
                    color = deckBackup.color
                )
            )
            deckBackup.cards.forEach { cardBackup ->
                cardRepository.insertCardWithId(
                    Card(
                        id = cardBackup.id,
                        deckId = deckId,
                        recto = cardBackup.recto,
                        verso = cardBackup.verso,
                        box = cardBackup.box,
                        isLearned = cardBackup.isLearned,
                        needsInput = cardBackup.needsInput,
                        nextReviewDate = cardBackup.nextReviewDate?.let { Instant.parse(it) },
                        lastReviewDate = cardBackup.lastReviewDate?.let { Instant.parse(it) },
                        rectoNormalized = cardBackup.rectoNormalized,
                        answerNormalized = cardBackup.answerNormalized
                    )
                )
            }
        }

        data.sessions.forEach { sessionBackup ->
            sessionRepository.insertSessionWithId(
                Session(
                    id = sessionBackup.id,
                    date = Instant.parse(sessionBackup.date),
                    deckIds = sessionBackup.deckIds,
                    cardCount = sessionBackup.cardCount,
                    successCount = sessionBackup.successCount,
                    masteredCount = sessionBackup.masteredCount,
                    advancedCount = sessionBackup.advancedCount,
                    retreatedCount = sessionBackup.retreatedCount,
                    isReported = sessionBackup.isReported
                )
            )
        }

        settingsRepository.setExcludedDays(
            data.settings.excludedDays.map { DayOfWeek.of(it) }.toSet()
        )
        settingsRepository.setTheme(AppTheme.valueOf(data.settings.theme))
        settingsRepository.setNotificationTime(
            LocalTime.of(data.settings.notificationHour, data.settings.notificationMinute)
        )
    }
}
