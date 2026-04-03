package com.imperadorsid.runningtracker.service

import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.buildTimerSteps
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.timer.SessionTimer
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.presentation.util.formatPhaseLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionManager(
    private val timer: SessionTimer = SessionTimer(),
    private var repository: SessionRepository? = null
) {

    val timerState: StateFlow<TimerState> = timer.timerState

    val intervalTransition: SharedFlow<IntervalType> = timer.intervalTransition

    fun setRepository(repository: SessionRepository) {
        this.repository = repository
    }

    fun handleAction(action: String, sessionId: Long = -1L, intervalsOnly: Boolean = false, scope: CoroutineScope) {
        when (action) {
            ACTION_START -> {
                if (sessionId != -1L) {
                    startSession(sessionId, intervalsOnly, scope)
                }
            }
            ACTION_PAUSE -> timer.pause()
            ACTION_RESUME -> timer.resume()
            ACTION_STOP -> timer.stop()
        }
    }

    fun stop() {
        timer.stop()
    }

    fun shouldUpdateNotification(state: TimerState.Running, lastStepIndex: Int?, lastPaused: Boolean?): Boolean {
        return state.currentStepIndex != lastStepIndex || state.isPaused != lastPaused
    }

    private fun startSession(sessionId: Long, intervalsOnly: Boolean, scope: CoroutineScope) {
        val repo = repository ?: return

        scope.launch {
            val session = repo.getSessionById(sessionId) ?: return@launch
            val steps = buildTimerSteps(session.patterns, intervalsOnly)
            timer.start(steps, scope)
        }
    }

    companion object {
        const val ACTION_START = "com.imperadorsid.runningtracker.ACTION_START"
        const val ACTION_PAUSE = "com.imperadorsid.runningtracker.ACTION_PAUSE"
        const val ACTION_RESUME = "com.imperadorsid.runningtracker.ACTION_RESUME"
        const val ACTION_STOP = "com.imperadorsid.runningtracker.ACTION_STOP"
    }
}
