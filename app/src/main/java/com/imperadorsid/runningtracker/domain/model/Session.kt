package com.imperadorsid.runningtracker.domain.model

data class Session(
    val id: Long = 0,
    val dateLabel: String,
    val patterns: List<IntervalPattern>,
    val createdAt: Long
) {
    val totalDurationSeconds: Int
        get() = WARMUP_SECONDS + patterns.sumOf { it.totalDurationSeconds } + COOLDOWN_SECONDS

    companion object {
        const val WARMUP_SECONDS = 600
        const val COOLDOWN_SECONDS = 300
    }
}
