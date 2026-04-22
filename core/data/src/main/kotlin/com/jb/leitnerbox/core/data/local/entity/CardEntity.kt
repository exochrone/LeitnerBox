package com.jb.leitnerbox.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jb.leitnerbox.core.domain.model.Card
import java.time.Instant

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val recto: String,
    val rectoNormalized: String,
    val verso: String,
    val answerNormalized: String,
    val box: Int,
    val lastReviewDate: Instant?,
    val nextReviewDate: Instant?,
    val isLearned: Boolean,
    val needsInput: Boolean = false
) {
    fun toDomain(): Card = Card(
        id = id,
        deckId = deckId,
        recto = recto,
        rectoNormalized = rectoNormalized,
        verso = verso,
        answerNormalized = answerNormalized,
        box = box,
        lastReviewDate = lastReviewDate,
        nextReviewDate = nextReviewDate,
        isLearned = isLearned,
        needsInput = needsInput
    )

    companion object {
        fun fromDomain(card: Card): CardEntity = CardEntity(
            id = card.id,
            deckId = card.deckId,
            recto = card.recto,
            rectoNormalized = card.rectoNormalized,
            verso = card.verso,
            answerNormalized = card.answerNormalized,
            box = card.box,
            lastReviewDate = card.lastReviewDate,
            nextReviewDate = card.nextReviewDate,
            isLearned = card.isLearned,
            needsInput = card.needsInput
        )
    }
}
