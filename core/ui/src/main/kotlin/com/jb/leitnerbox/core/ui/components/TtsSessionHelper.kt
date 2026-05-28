package com.jb.leitnerbox.core.ui.components

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.utils.LatexDetector

object TtsSessionHelper {
    /**
     * Centralise la logique de lecture selon l'état de la carte pour éviter la duplication.
     */
    fun resolveTextAndSpeak(
        card: Card?,
        isFlipped: Boolean,
        isTtsEnabled: Boolean,
        onSpeakRequest: (String) -> Unit
    ) {
        if (!isTtsEnabled || card == null) return
        val textToSpeak = if (isFlipped) card.verso else card.recto
        
        // US-SESSION2-TTS: Ne pas lire si contient du LaTeX
        if (LatexDetector.containsLatex(textToSpeak)) return

        onSpeakRequest(textToSpeak)
    }
}
