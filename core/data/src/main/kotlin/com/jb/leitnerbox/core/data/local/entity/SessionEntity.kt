package com.jb.leitnerbox.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jb.leitnerbox.core.domain.model.Session
import java.time.Instant

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Instant,
    val deckIds: List<Long>,
    val cardCount: Int,
    val successCount: Int,
    val masteredCount: Int,
    val advancedCount: Int,
    val retreatedCount: Int,
    val isReported: Boolean
)

fun SessionEntity.toDomain() = Session(
    id = id,
    date = date,
    deckIds = deckIds,
    cardCount = cardCount,
    successCount = successCount,
    masteredCount = masteredCount,
    advancedCount = advancedCount,
    retreatedCount = retreatedCount,
    isReported = isReported
)

fun Session.toEntity() = SessionEntity(
    id = id,
    date = date,
    deckIds = deckIds,
    cardCount = cardCount,
    successCount = successCount,
    masteredCount = masteredCount,
    advancedCount = advancedCount,
    retreatedCount = retreatedCount,
    isReported = isReported
)
