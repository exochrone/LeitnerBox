package com.jb.leitnerbox.core.domain.utils

import java.text.Normalizer

class AnswerNormalizer {
    fun normalize(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "") // Supprime accents
            .lowercase()
            .replace("[^a-z0-9\\s]".toRegex(), "") // Ne garde que alphanumérique et espaces
            .trim()
            .replace("\\s+".toRegex(), " ") // Espaces multiples -> un seul
    }
}
