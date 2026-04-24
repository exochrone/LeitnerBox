package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.AnswerCheckResult
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlin.math.floor

class CheckAnswerUseCase(
    private val answerNormalizer: AnswerNormalizer
) {

    operator fun invoke(card: Card, userInput: String): AnswerCheckResult {
        if (!card.needsInput || card.verso.isEmpty()) return AnswerCheckResult.AutoCheckDisabled
        if (userInput.isEmpty()) return AnswerCheckResult.Incorrect

        val normalizedExpected = card.answerNormalized
        val normalizedActual = answerNormalizer.normalize(userInput)

        val isCorrect = if (normalizedExpected.length <= 4) {
            normalizedExpected == normalizedActual
        } else {
            val threshold = floor(normalizedExpected.length / 5.0).toInt()
            val distance = levenshtein(normalizedExpected, normalizedActual)
            distance <= threshold
        }

        return if (isCorrect) AnswerCheckResult.Correct else AnswerCheckResult.Incorrect
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
