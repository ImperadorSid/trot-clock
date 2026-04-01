package com.imperadorsid.runningtracker.domain.timer

import com.imperadorsid.runningtracker.domain.model.TimerStep

sealed class TimerState {
    data object Idle : TimerState()

    data class Running(
        val currentStepIndex: Int,
        val currentStep: TimerStep,
        val remainingSeconds: Int,
        val totalRemainingSeconds: Int,
        val isPaused: Boolean = false
    ) : TimerState()

    data object Completed : TimerState()
}
