package com.imperadorsid.runningtracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerStepTest {

    @Test
    fun `buildTimerSteps starts with warmup walk`() {
        val patterns = listOf(
            IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 90)
        )
        val steps = buildTimerSteps(patterns)
        val first = steps.first()
        assertEquals(TimerPhase.WARMUP, first.phase)
        assertEquals(IntervalType.WALK, first.type)
        assertEquals(Session.WARMUP_SECONDS, first.durationSeconds)
    }

    @Test
    fun `buildTimerSteps ends with cooldown walk`() {
        val patterns = listOf(
            IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 90)
        )
        val steps = buildTimerSteps(patterns)
        val last = steps.last()
        assertEquals(TimerPhase.COOLDOWN, last.phase)
        assertEquals(IntervalType.WALK, last.type)
        assertEquals(Session.COOLDOWN_SECONDS, last.durationSeconds)
    }

    @Test
    fun `buildTimerSteps single pattern produces correct sequence`() {
        val patterns = listOf(
            IntervalPattern(reps = 2, walkDurationSeconds = 60, jogDurationSeconds = 90)
        )
        val steps = buildTimerSteps(patterns)
        // warmup, walk, jog, walk, jog, cooldown
        assertEquals(6, steps.size)

        assertEquals(TimerStep(Session.WARMUP_SECONDS, IntervalType.WALK, TimerPhase.WARMUP), steps[0])
        assertEquals(TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE), steps[1])
        assertEquals(TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE), steps[2])
        assertEquals(TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE), steps[3])
        assertEquals(TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE), steps[4])
        assertEquals(TimerStep(Session.COOLDOWN_SECONDS, IntervalType.WALK, TimerPhase.COOLDOWN), steps[5])
    }

    @Test
    fun `buildTimerSteps multiple patterns chains correctly`() {
        val patterns = listOf(
            IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 90),
            IntervalPattern(reps = 1, walkDurationSeconds = 120, jogDurationSeconds = 60)
        )
        val steps = buildTimerSteps(patterns)
        // warmup, (walk60, jog90), (walk120, jog60), cooldown
        assertEquals(6, steps.size)

        assertEquals(TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE), steps[1])
        assertEquals(TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE), steps[2])
        assertEquals(TimerStep(120, IntervalType.WALK, TimerPhase.ACTIVE), steps[3])
        assertEquals(TimerStep(60, IntervalType.JOG, TimerPhase.ACTIVE), steps[4])
    }

    @Test
    fun `buildTimerSteps with no patterns has only warmup and cooldown`() {
        val steps = buildTimerSteps(emptyList())
        assertEquals(2, steps.size)
        assertEquals(TimerPhase.WARMUP, steps[0].phase)
        assertEquals(TimerPhase.COOLDOWN, steps[1].phase)
    }

    @Test
    fun `buildTimerSteps with intervalsOnly omits warmup and cooldown`() {
        val patterns = listOf(
            IntervalPattern(reps = 2, walkDurationSeconds = 60, jogDurationSeconds = 90)
        )
        val steps = buildTimerSteps(patterns, intervalsOnly = true)
        // walk, jog, walk, jog (no warmup, no cooldown)
        assertEquals(4, steps.size)
        assertEquals(TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE), steps[0])
        assertEquals(TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE), steps[1])
        assertEquals(TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE), steps[2])
        assertEquals(TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE), steps[3])
    }

    @Test
    fun `buildTimerSteps with intervalsOnly and no patterns is empty`() {
        val steps = buildTimerSteps(emptyList(), intervalsOnly = true)
        assertEquals(0, steps.size)
    }
}
