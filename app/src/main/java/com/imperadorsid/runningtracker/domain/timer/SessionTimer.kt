package com.imperadorsid.runningtracker.domain.timer

import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.TimerStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionTimer {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState

    private val _intervalTransition = MutableSharedFlow<IntervalType>()
    val intervalTransition: SharedFlow<IntervalType> = _intervalTransition

    private val _isPaused = MutableStateFlow(false)
    private var timerJob: Job? = null

    fun start(steps: List<TimerStep>, scope: CoroutineScope) {
        stop()
        if (steps.isEmpty()) return

        timerJob = scope.launch {
            val totalSeconds = steps.sumOf { it.durationSeconds }
            var elapsedTotal = 0

            for ((index, step) in steps.withIndex()) {
                _timerState.value = TimerState.Running(
                    currentStepIndex = index,
                    currentStep = step,
                    remainingSeconds = step.durationSeconds,
                    totalRemainingSeconds = totalSeconds - elapsedTotal
                )

                if (index > 0) {
                    _intervalTransition.emit(step.type)
                }

                for (elapsed in 0 until step.durationSeconds) {
                    _isPaused.first { !it }
                    delay(1000)
                    _isPaused.first { !it }
                    elapsedTotal++
                    _timerState.value = TimerState.Running(
                        currentStepIndex = index,
                        currentStep = step,
                        remainingSeconds = step.durationSeconds - elapsed - 1,
                        totalRemainingSeconds = totalSeconds - elapsedTotal
                    )
                }
            }

            _timerState.value = TimerState.Completed
        }
    }

    fun pause() {
        _isPaused.value = true
        val current = _timerState.value
        if (current is TimerState.Running) {
            _timerState.value = current.copy(isPaused = true)
        }
    }

    fun resume() {
        _isPaused.value = false
        val current = _timerState.value
        if (current is TimerState.Running) {
            _timerState.value = current.copy(isPaused = false)
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _isPaused.value = false
        _timerState.value = TimerState.Idle
    }
}
