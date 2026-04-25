package com.jb.leitnerbox.feature.session.ui.result

import androidx.lifecycle.ViewModel
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionResultViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    val session: Session
        get() = sessionStateHolder.lastSessionResult
            ?: error("No session result available")

    val successRate: Int
        get() = if (session.cardCount == 0) 0
        else (session.successCount * 100) / session.cardCount
}
