package com.jb.leitnerbox.core.domain.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = BACKUP_VERSION,
    val exportDate: String,
    val appVersion: String,
    val decks: List<DeckBackup>,
    val sessions: List<SessionBackup>,
    val settings: SettingsBackup
)

@Serializable
data class DeckBackup(
    val id: Long,
    val name: String,
    val description: String,
    val intervals: List<Int>,
    val wrongAnswerRule: String,
    val presentationOrder: String,
    val color: String,
    val cards: List<CardBackup>
)

@Serializable
data class CardBackup(
    val id: Long,
    val recto: String,
    val verso: String,
    val box: Int,
    val isLearned: Boolean,
    val needsInput: Boolean,
    val nextReviewDate: String?,
    val lastReviewDate: String?,
    val rectoNormalized: String,
    val answerNormalized: String
)

@Serializable
data class SessionBackup(
    val id: Long,
    val date: String,
    val deckIds: List<Long>,
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int,
    val advancedCount: Int,
    val retreatedCount: Int,
    val isReported: Boolean
)

@Serializable
data class SettingsBackup(
    val excludedDays: List<Int>,
    val theme: String,
    val notificationHour: Int,
    val notificationMinute: Int
)

const val BACKUP_VERSION = 1
