package com.jb.leitnerbox.core.data.local.entity

import androidx.room.Embedded

data class DeckWithCount(
    @Embedded val deck: DeckEntity,
    val cardCount: Int
)
