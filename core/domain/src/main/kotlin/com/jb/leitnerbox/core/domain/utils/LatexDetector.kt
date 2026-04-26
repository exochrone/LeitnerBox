package com.jb.leitnerbox.core.domain.utils

object LatexDetector {
    private val blockPattern  = Regex("""\$\$[^$]+\$\$""")
    private val inlinePattern = Regex("""\$[^$\s][^$]*[^$\s]\$|\$\S\$""")

    /**
     * Retourne true si le texte contient au moins un délimiteur LaTeX
     * ($...$ ou $$...$$).
     */
    fun containsLatex(text: String): Boolean =
        blockPattern.containsMatchIn(text) || inlinePattern.containsMatchIn(text)
}
