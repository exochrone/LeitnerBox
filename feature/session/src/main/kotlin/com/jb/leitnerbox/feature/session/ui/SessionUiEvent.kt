package com.jb.leitnerbox.feature.session.ui

sealed class SessionUiEvent {
    data object CardMastered : SessionUiEvent()
    data object SessionFinished : SessionUiEvent()
}
