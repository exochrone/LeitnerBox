package com.jb.leitnerbox.core.domain.utils

object LatexDetector {
    /**
     * Retourne true si le texte contient au moins un délimiteur LaTeX
     * ($...$ ou $$...$$).
     */
    fun containsLatex(text: String): Boolean =
        text.contains('$')
}
