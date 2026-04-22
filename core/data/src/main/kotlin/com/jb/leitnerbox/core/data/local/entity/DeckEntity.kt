package com.jb.leitnerbox.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import java.time.DayOfWeek

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val intervals: List<Int>,
    val excludedDays: Set<DayOfWeek>,
    val backToFirstOnFail: Boolean,
    val presentationOrder: PresentationOrder
) {
    fun toDomain(): Deck = Deck(
        id = id,
        name = name,
        description = description,
        intervals = intervals,
        excludedDays = excludedDays,
        backToFirstOnFail = backToFirstOnFail,
        presentationOrder = presentationOrder
    )

    companion object {
        fun fromDomain(deck: Deck): DeckEntity = DeckEntity(
            id = deck.id,
            name = deck.name,
            description = deck.description,
            intervals = deck.intervals,
            excludedDays = deck.excludedDays,
            backToFirstOnFail = deck.backToFirstOnFail,
            presentationOrder = deck.presentationOrder
        )
    }
}
