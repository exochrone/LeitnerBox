package com.jb.leitnerbox.core.domain.model

sealed class AnswerCheckResult {
    data object Correct : AnswerCheckResult()
    data object Incorrect : AnswerCheckResult()
    data object AutoCheckDisabled : AnswerCheckResult()
}
