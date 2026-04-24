package com.jb.leitnerbox.feature.session.ui

sealed class SessionUiEvent {
    object SessionFinished : SessionUiEvent()
}
