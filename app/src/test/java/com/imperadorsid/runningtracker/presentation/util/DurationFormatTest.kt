package com.imperadorsid.runningtracker.presentation.util

import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.model.TimerStep
import com.imperadorsid.runningtracker.domain.timer.TimerState
import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatTest {

    @Test
    fun `formatDuration with zero seconds`() {
        assertEquals("0:00", formatDuration(0))
    }

    @Test
    fun `formatDuration with 90 seconds`() {
        assertEquals("1:30", formatDuration(90))
    }

    @Test
    fun `formatDuration with 600 seconds`() {
        assertEquals("10:00", formatDuration(600))
    }

    @Test
    fun `formatDuration with 5 seconds pads with zero`() {
        assertEquals("0:05", formatDuration(5))
    }

    @Test
    fun `formatDurationLong with even minutes`() {
        assertEquals("10m", formatDurationLong(600))
    }

    @Test
    fun `formatDurationLong with minutes and seconds`() {
        assertEquals("1m 30s", formatDurationLong(90))
    }

    @Test
    fun `formatDurationLong with zero`() {
        assertEquals("0m", formatDurationLong(0))
    }

    @Test
    fun `formatPhaseLabel warmup returns Warmup`() {
        val state = TimerState.Running(
            currentStepIndex = 0,
            currentStep = TimerStep(600, IntervalType.WALK, TimerPhase.WARMUP),
            remainingSeconds = 300,
            totalRemainingSeconds = 900
        )
        assertEquals("Warmup", formatPhaseLabel(state))
    }

    @Test
    fun `formatPhaseLabel active walk returns WALK`() {
        val state = TimerState.Running(
            currentStepIndex = 1,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 30,
            totalRemainingSeconds = 600
        )
        assertEquals("WALK", formatPhaseLabel(state))
    }

    @Test
    fun `formatPhaseLabel active jog returns JOG`() {
        val state = TimerState.Running(
            currentStepIndex = 2,
            currentStep = TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE),
            remainingSeconds = 45,
            totalRemainingSeconds = 500
        )
        assertEquals("JOG", formatPhaseLabel(state))
    }

    @Test
    fun `formatPhaseLabel cooldown returns Cooldown`() {
        val state = TimerState.Running(
            currentStepIndex = 5,
            currentStep = TimerStep(300, IntervalType.WALK, TimerPhase.COOLDOWN),
            remainingSeconds = 150,
            totalRemainingSeconds = 150
        )
        assertEquals("Cooldown", formatPhaseLabel(state))
    }
}