package com.jb.leitnerbox.core.domain.usecase.card

import java.text.Normalizer
import kotlin.math.floor

class ValidateResponseUseCase {

    operator fun invoke(expected: String, actual: String): Boolean {
        if (expected.isEmpty()) return true // Si attendu vide, on considère OK (devrait pas arriver avec saisieRequise=true)
        if (actual.isEmpty()) return false

        val normalizedExpected = normalize(expected)
        val normalizedActual = normalize(actual)

        if (normalizedExpected.length <= 4) {
            return normalizedExpected == normalizedActual
        }

        val threshold = floor(normalizedExpected.length / 5.0).toInt()
        val distance = levenshtein(normalizedExpected, normalizedActual)

        return distance <= threshold
    }

    fun normalize(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "") // Supprime accents
            .lowercase()
            .replace("[^a-z0-9\\s]".toRegex(), "") // Ne garde que alphanumérique et espaces
            .trim()
            .replace("\\s+".toRegex(), " ") // Espaces multiples -> un seul
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}