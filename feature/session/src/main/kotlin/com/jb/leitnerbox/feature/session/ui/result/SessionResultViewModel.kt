package com.jb.leitnerbox.feature.session.ui.result

import androidx.lifecycle.ViewModel
import com.jb.leitnerbox.core.domain.model.Session
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SessionResultViewModel @Inject constructor(
    private val sessionStateHolder: SessionStateHolder
) : ViewModel() {

    private val _showCelebration = MutableStateFlow(false)
    val showCelebration = _showCelebration.asStateFlow()

    init {
        if (successRate >= 60) {
            _showCelebration.value = true
        }
    }

    fun onCelebrationFinished() {
        _showCelebration.value = false
    }

    val session: Session
        get() = sessionStateHolder.lastSessionResult
            ?: error("No session result available")

    val successRate: Int
        get() = if (session.cardCount == 0) 0
        else (session.successCount * 100) / session.cardCount
}
