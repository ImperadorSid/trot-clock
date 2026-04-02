package com.imperadorsid.runningtracker.domain.model

enum class TimerPhase {
    WARMUP,
    ACTIVE,
    COOLDOWN
}

data class TimerStep(
    val durationSeconds: Int,
    val type: IntervalType,
    val phase: TimerPhase
)

fun buildTimerSteps(patterns: List<IntervalPattern>, intervalsOnly: Boolean = false): List<TimerStep> {
    val steps = mutableListOf<TimerStep>()

    if (!intervalsOnly) {
        steps.add(TimerStep(Session.WARMUP_SECONDS, IntervalType.WALK, TimerPhase.WARMUP))
    }

    for (pattern in patterns) {
        repeat(pattern.reps) {
            steps.add(TimerStep(pattern.walkDurationSeconds, IntervalType.WALK, TimerPhase.ACTIVE))
            steps.add(TimerStep(pattern.jogDurationSeconds, IntervalType.JOG, TimerPhase.ACTIVE))
        }
    }

    if (!intervalsOnly) {
        steps.add(TimerStep(Session.COOLDOWN_SECONDS, IntervalType.WALK, TimerPhase.COOLDOWN))
    }

    return steps
}
