package com.jb.leitnerbox.feature.session.ui.extra

import androidx.lifecycle.ViewModel
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExtraSessionResultViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    val cardCount: Int
        get() = sessionStateHolder.extraSessionCardCount

    val successCount: Int
        get() = sessionStateHolder.extraSessionSuccessCount

    val successRate: Int
        get() = if (cardCount == 0) 0
                else (successCount * 100) / cardCount

    val shouldCelebrate: Boolean
        get() = successRate >= 60
}
