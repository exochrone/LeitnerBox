package com.jb.leitnerbox.core.domain.model

import java.time.Instant

data class Card(
    val id: Long = 0,
    val deckId: Long,
    val recto: String,
    val verso: String,
    val box: Int = 1, // 1 to 7
    val lastReviewDate: Instant? = null,
    val nextReviewDate: Instant? = null,
    val isLearned: Boolean = false,
    val needsInput: Boolean = false // P3-UI-22 : Saisie requise
)