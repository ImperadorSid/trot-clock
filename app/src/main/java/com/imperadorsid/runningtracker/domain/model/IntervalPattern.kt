package com.imperadorsid.runningtracker.domain.model

data class IntervalPattern(
    val id: Long = 0,
    val reps: Int,
    val walkDurationSeconds: Int,
    val jogDurationSeconds: Int
) {
    val totalDurationSeconds: Int
        get() = reps * (walkDurationSeconds + jogDurationSeconds)
}
