package com.imperadorsid.runningtracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionTest {

    @Test
    fun `totalDurationSeconds includes warmup, patterns, and cooldown`() {
        val session = Session(
            dateLabel = "01/04",
            patterns = listOf(
                IntervalPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
            ),
            createdAt = 0L
        )
        // 600 warmup + 3*(60+90) + 300 cooldown = 600 + 450 + 300 = 1350
        assertEquals(1350, session.totalDurationSeconds)
    }

    @Test
    fun `totalDurationSeconds with multiple patterns`() {
        val session = Session(
            dateLabel = "01/04",
            patterns = listOf(
                IntervalPattern(reps = 3, walkDurationSeconds = 120, jogDurationSeconds = 120),
                IntervalPattern(reps = 3, walkDurationSeconds = 180, jogDurationSeconds = 120)
            ),
            createdAt = 0L
        )
        // 600 + 3*(120+120) + 3*(180+120) + 300 = 600 + 720 + 900 + 300 = 2520
        assertEquals(2520, session.totalDurationSeconds)
    }

    @Test
    fun `totalDurationSeconds with no patterns still has warmup and cooldown`() {
        val session = Session(
            dateLabel = "01/04",
            patterns = emptyList(),
            createdAt = 0L
        )
        assertEquals(Session.WARMUP_SECONDS + Session.COOLDOWN_SECONDS, session.totalDurationSeconds)
    }

    @Test
    fun `warmup is 600 seconds and cooldown is 300 seconds`() {
        assertEquals(600, Session.WARMUP_SECONDS)
        assertEquals(300, Session.COOLDOWN_SECONDS)
    }
}
