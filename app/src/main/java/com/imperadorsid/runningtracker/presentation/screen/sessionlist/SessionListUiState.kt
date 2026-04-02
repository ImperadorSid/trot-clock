package com.imperadorsid.runningtracker.presentation.screen.sessionlist

import com.imperadorsid.runningtracker.domain.model.Session

sealed interface SessionListUiState {
    data object Loading : SessionListUiState
    data class Success(val sessions: List<Session>) : SessionListUiState
}
