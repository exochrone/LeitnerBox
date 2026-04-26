package com.jb.leitnerbox.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import java.time.DayOfWeek

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val intervals: List<Int>,
    val wrongAnswerRule: WrongAnswerRule,
    val presentationOrder: PresentationOrder,
    val color: String = "default"
) {
    fun toDomain(): Deck = Deck(
        id = id,
        name = name,
        description = description,
        intervals = intervals,
        wrongAnswerRule = wrongAnswerRule,
        presentationOrder = presentationOrder,
        color = color
    )

    companion object {
        fun fromDomain(deck: Deck): DeckEntity = DeckEntity(
            id = deck.id,
            name = deck.name,
            description = deck.description,
            intervals = deck.intervals,
            wrongAnswerRule = deck.wrongAnswerRule,
            presentationOrder = deck.presentationOrder,
            color = deck.color
        )
    }
}
