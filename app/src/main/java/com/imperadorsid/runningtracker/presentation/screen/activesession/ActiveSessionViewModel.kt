package com.imperadorsid.runningtracker.presentation.screen.activesession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.service.RunTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActiveSessionViewModel(
    private val sessionId: Long,
    private val repository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActiveSessionUiState>(ActiveSessionUiState.Loading)
    val uiState: StateFlow<ActiveSessionUiState> = _uiState

    init {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session == null) {
                _uiState.value = ActiveSessionUiState.Error
                return@launch
            }

            _uiState.value = ActiveSessionUiState.Ready(session, TimerState.Idle)

            RunTrackingService.timerState.collect { timerState ->
                _uiState.value = ActiveSessionUiState.Ready(session, timerState)
            }
        }
    }

    companion object {
        fun factory(sessionId: Long, repository: SessionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ActiveSessionViewModel(sessionId, repository) as T
                }
            }
    }
}