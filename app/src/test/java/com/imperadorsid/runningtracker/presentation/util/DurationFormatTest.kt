package com.imperadorsid.runningtracker.presentation.util

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
}