package com.imperadorsid.runningtracker.presentation.screen.sessionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionListViewModel(
    private val repository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<SessionListUiState> = repository.getSessions()
        .map { sessions -> SessionListUiState.Success(sessions) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionListUiState.Loading)

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    companion object {
        fun factory(repository: SessionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SessionListViewModel(repository) as T
                }
            }
    }
}