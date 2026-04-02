package com.imperadorsid.runningtracker.presentation.screen.activesession

import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.timer.TimerState

sealed interface ActiveSessionUiState {
    data object Loading : ActiveSessionUiState
    data class Ready(val session: Session, val timerState: TimerState) : ActiveSessionUiState
    data object Error : ActiveSessionUiState
}
