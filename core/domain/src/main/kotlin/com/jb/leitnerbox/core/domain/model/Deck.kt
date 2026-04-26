package com.jb.leitnerbox.core.domain.model

import java.time.DayOfWeek

data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val intervals: List<Int> = listOf(1, 3, 5, 7, 14), // Par défaut 5 boîtes
    val wrongAnswerRule: WrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
    val presentationOrder: PresentationOrder = PresentationOrder.RANDOM,
    val color: String = "default"
)

enum class PresentationOrder {
    RANDOM, CREATION_DATE, BY_BOX
}