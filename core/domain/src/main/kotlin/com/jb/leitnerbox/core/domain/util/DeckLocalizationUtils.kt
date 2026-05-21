package com.jb.leitnerbox.core.domain.util

object DeckLocalizationUtils {
    /**
     * Retourne le mot "deck" correctement accordé selon la quantité fournie.
     * Règle : prend un 's' si quantité > 1.
     */
    fun getDeckLabel(quantity: Int): String {
        return if (quantity > 1) "decks" else "deck"
    }
}
