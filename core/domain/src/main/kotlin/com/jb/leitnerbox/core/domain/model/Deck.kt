package com.jb.leitnerbox.core.domain.model

import java.time.DayOfWeek

data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val intervals: List<Int> = listOf(1, 3, 5, 7, 14), // Par défaut 5 boîtes
    val wrongAnswerRule: WrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
    val presentationOrder: PresentationOrder = PresentationOrder.RANDOM,
    val color: String = DEFAULT_COLOR
) {
    companion object {
        const val DEFAULT_COLOR = "default"

        val AVAILABLE_COLORS = listOf(
            "#8B0000", // Rouge sombre
            "#B22222", // Rouge brique
            "#D2691E", // Chocolat
            "#DAA520", // Or foncé
            "#2E8B57", // Vert mer
            "#006400", // Vert forêt
            "#008B8B", // Cyan sombre
            "#1E4FBA", // Bleu marine
            "#6A0DAD", // Indigo
            "#800080", // Violet
            "#C71585", // Rose vif
            "#696969"  // Gris ardoise
        )
    }
}

enum class PresentationOrder {
    RANDOM, CREATION_DATE, BY_BOX
}