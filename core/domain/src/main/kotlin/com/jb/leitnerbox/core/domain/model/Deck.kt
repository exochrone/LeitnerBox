package com.jb.leitnerbox.core.domain.model

import java.time.DayOfWeek

data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val intervals: List<Int> = listOf(1, 2, 4, 7, 15, 30, 60), // Par défaut 7 boîtes
    val wrongAnswerRule: WrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
    val presentationOrder: PresentationOrder = PresentationOrder.RANDOM
)

enum class PresentationOrder {
    RANDOM, CREATION_DATE, BY_BOX
}