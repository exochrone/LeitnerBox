package com.jb.leitnerbox.feature.session.ui

sealed class SessionUiEvent {
    data object CardMastered : SessionUiEvent()
    data object SessionFinished : SessionUiEvent()
    data class SpeakText(val text: String) : SessionUiEvent()
    data object NavigateBack : SessionUiEvent()
}
