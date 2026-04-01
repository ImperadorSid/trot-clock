package com.imperadorsid.runningtracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class IntervalPatternTest {

    @Test
    fun `totalDurationSeconds equals reps times walk plus jog`() {
        val pattern = IntervalPattern(
            reps = 5,
            walkDurationSeconds = 60,
            jogDurationSeconds = 90
        )
        assertEquals(5 * (60 + 90), pattern.totalDurationSeconds)
    }

    @Test
    fun `totalDurationSeconds with single rep`() {
        val pattern = IntervalPattern(
            reps = 1,
            walkDurationSeconds = 120,
            jogDurationSeconds = 60
        )
        assertEquals(180, pattern.totalDurationSeconds)
    }

    @Test
    fun `IntervalType has WALK and JOG values`() {
        val values = IntervalType.entries
        assertEquals(2, values.size)
        assertEquals(IntervalType.WALK, values[0])
        assertEquals(IntervalType.JOG, values[1])
    }
}
