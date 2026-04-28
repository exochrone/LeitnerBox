package com.jb.leitnerbox.feature.dashboard.ui

import com.jb.leitnerbox.core.domain.model.SessionPlan
import com.jb.leitnerbox.core.domain.model.DashboardGlobalStats
import java.time.Instant

data class DashboardUiState(
    val sessionPlan: SessionPlan = SessionPlan(Instant.now(), emptyList()),
    val stats: DashboardGlobalStats? = null,
    val masteredCardCount: Int = 0,
    val isLoading: Boolean = true
)
