package com.imperadorsid.runningtracker.presentation.util

import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.timer.TimerState

fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

fun formatDurationLong(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"
}

fun formatPhaseLabel(state: TimerState.Running): String {
    return when (state.currentStep.phase) {
        TimerPhase.WARMUP -> "Warmup"
        TimerPhase.ACTIVE -> state.currentStep.type.name
        TimerPhase.COOLDOWN -> "Cooldown"
    }
}
