package com.jb.leitnerbox.core.domain.model

import java.time.DayOfWeek

data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val intervals: List<Int> = listOf(1, 2, 4, 7, 15, 30, 60), // Par défaut 7 boîtes
    val excludedDays: Set<DayOfWeek> = emptySet(),
    val backToFirstOnFail: Boolean = true, // true: boîte 1, false: boîte précédente
    val presentationOrder: PresentationOrder = PresentationOrder.RANDOM,
    val cardCount: Int = 0 // Ajouté pour l'affichage dans la liste
)

enum class PresentationOrder {
    RANDOM, CREATION_DATE, BY_BOX
}