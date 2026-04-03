package com.imperadorsid.runningtracker.domain.timer

import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.model.TimerStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerStateTest {

    @Test
    fun `Idle is singleton`() {
        assertTrue(TimerState.Idle === TimerState.Idle)
    }

    @Test
    fun `Completed is singleton`() {
        assertTrue(TimerState.Completed === TimerState.Completed)
    }

    @Test
    fun `Running defaults to not paused`() {
        val state = TimerState.Running(
            currentStepIndex = 0,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 60,
            totalRemainingSeconds = 120
        )
        assertFalse(state.isPaused)
    }

    @Test
    fun `Running copy preserves fields`() {
        val step = TimerStep(60, IntervalType.JOG, TimerPhase.ACTIVE)
        val state = TimerState.Running(
            currentStepIndex = 2,
            currentStep = step,
            remainingSeconds = 30,
            totalRemainingSeconds = 90
        )
        val paused = state.copy(isPaused = true)
        assertEquals(2, paused.currentStepIndex)
        assertEquals(step, paused.currentStep)
        assertEquals(30, paused.remainingSeconds)
        assertEquals(90, paused.totalRemainingSeconds)
        assertTrue(paused.isPaused)
    }

    @Test
    fun `Running equality works`() {
        val step = TimerStep(60, IntervalType.WALK, TimerPhase.WARMUP)
        val a = TimerState.Running(0, step, 60, 120)
        val b = TimerState.Running(0, step, 60, 120)
        assertEquals(a, b)
    }
}
