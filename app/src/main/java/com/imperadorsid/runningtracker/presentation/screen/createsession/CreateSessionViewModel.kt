package com.imperadorsid.runningtracker.presentation.screen.createsession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.util.Clock
import com.imperadorsid.runningtracker.domain.util.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateSessionViewModel(
    private val repository: SessionRepository,
    private val clock: Clock = SystemClock
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSessionUiState())
    val uiState: StateFlow<CreateSessionUiState> = _uiState

    fun addPattern(reps: Int, walkDurationSeconds: Int, jogDurationSeconds: Int) {
        val pattern = IntervalPattern(
            reps = reps,
            walkDurationSeconds = walkDurationSeconds,
            jogDurationSeconds = jogDurationSeconds
        )
        _uiState.value = _uiState.value.copy(
            patterns = _uiState.value.patterns + pattern,
            validationError = null
        )
    }

    fun removePattern(index: Int) {
        val updated = _uiState.value.patterns.toMutableList()
        updated.removeAt(index)
        _uiState.value = _uiState.value.copy(patterns = updated)
    }

    fun saveSession() {
        val current = _uiState.value
        if (current.patterns.isEmpty()) {
            _uiState.value = current.copy(validationError = "Add at least one pattern")
            return
        }

        _uiState.value = current.copy(isSaving = true, validationError = null)

        viewModelScope.launch {
            val now = clock.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val dateLabel = dateFormat.format(Date(now))

            val session = Session(
                dateLabel = dateLabel,
                patterns = current.patterns,
                createdAt = now
            )
            val id = repository.insertSession(session)
            _uiState.value = _uiState.value.copy(isSaving = false, savedSessionId = id)
        }
    }

    companion object {
        fun factory(repository: SessionRepository, clock: Clock = SystemClock): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CreateSessionViewModel(repository, clock) as T
                }
            }
    }
}
