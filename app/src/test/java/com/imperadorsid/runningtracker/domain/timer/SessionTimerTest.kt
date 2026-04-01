package com.imperadorsid.runningtracker.domain.timer

import app.cash.turbine.test
import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.model.TimerStep
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerTest {

    private lateinit var timer: SessionTimer

    @Before
    fun setup() {
        timer = SessionTimer()
    }

    private val simpleSteps = listOf(
        TimerStep(3, IntervalType.WALK, TimerPhase.WARMUP),
        TimerStep(2, IntervalType.WALK, TimerPhase.ACTIVE),
        TimerStep(2, IntervalType.JOG, TimerPhase.ACTIVE),
        TimerStep(3, IntervalType.WALK, TimerPhase.COOLDOWN)
    )

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(TimerState.Idle, timer.timerState.value)
    }

    @Test
    fun `start transitions to Running with first step`() = runTest {
        timer.timerState.test {
            assertEquals(TimerState.Idle, awaitItem())

            timer.start(simpleSteps, this@runTest)
            val state = awaitItem() as TimerState.Running
            assertEquals(0, state.currentStepIndex)
            assertEquals(TimerPhase.WARMUP, state.currentStep.phase)
            assertEquals(IntervalType.WALK, state.currentStep.type)
            assertEquals(3, state.remainingSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `countdown decrements remaining seconds`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            val initial = awaitItem() as TimerState.Running
            assertEquals(3, initial.remainingSeconds)

            advanceTimeBy(1001)
            val after1s = awaitItem() as TimerState.Running
            assertEquals(2, after1s.remainingSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `transitions to next step when current step finishes`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            awaitItem() // warmup start

            // Advance past warmup (3s)
            advanceTimeBy(3001)
            var state: TimerState = awaitItem()
            while (state is TimerState.Running && state.currentStepIndex == 0) {
                state = awaitItem()
            }
            assertTrue(state is TimerState.Running)
            val running = state as TimerState.Running
            assertEquals(1, running.currentStepIndex)
            assertEquals(TimerPhase.ACTIVE, running.currentStep.phase)
            assertEquals(IntervalType.WALK, running.currentStep.type)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `pause stops countdown`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            awaitItem() // warmup start, 3s

            advanceTimeBy(1001)
            awaitItem() // 2s remaining

            timer.pause()

            // Consume all pending emissions until we see isPaused=true
            var state = awaitItem()
            while (state is TimerState.Running && !state.isPaused) {
                state = awaitItem()
            }
            assertTrue(state is TimerState.Running)
            assertTrue((state as TimerState.Running).isPaused)
            val pausedRemaining = state.remainingSeconds

            // Advance time — timer should not progress
            advanceTimeBy(3000)

            val current = timer.timerState.value as TimerState.Running
            assertEquals(pausedRemaining, current.remainingSeconds)

            // Stop the timer so its coroutine finishes — otherwise runTest
            // waits 1 minute for the paused coroutine and fails
            timer.stop()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `resume continues countdown after pause`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            awaitItem() // warmup start, 3s

            advanceTimeBy(1001)
            awaitItem() // 2s remaining

            timer.pause()
            awaitItem() // paused

            timer.resume()
            val resumed = awaitItem() as TimerState.Running
            assertEquals(false, resumed.isPaused)
            assertEquals(2, resumed.remainingSeconds)

            advanceTimeBy(1001)
            val after = awaitItem() as TimerState.Running
            assertEquals(1, after.remainingSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `stop resets to Idle`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            awaitItem() // Running

            timer.stop()
            assertEquals(TimerState.Idle, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `completes after all steps finish`() = runTest {
        val shortSteps = listOf(
            TimerStep(1, IntervalType.WALK, TimerPhase.WARMUP),
            TimerStep(1, IntervalType.JOG, TimerPhase.ACTIVE),
            TimerStep(1, IntervalType.WALK, TimerPhase.COOLDOWN)
        )

        timer.timerState.test {
            awaitItem() // Idle

            timer.start(shortSteps, this@runTest)
            awaitItem() // Running

            advanceTimeBy(4001)
            var state: TimerState = awaitItem()
            while (state !is TimerState.Completed) {
                state = awaitItem()
            }
            assertEquals(TimerState.Completed, state)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `totalRemainingSeconds tracks overall progress`() = runTest {
        timer.timerState.test {
            awaitItem() // Idle

            timer.start(simpleSteps, this@runTest)
            val initial = awaitItem() as TimerState.Running
            assertEquals(10, initial.totalRemainingSeconds) // 3+2+2+3

            advanceTimeBy(1001)
            val after1s = awaitItem() as TimerState.Running
            assertEquals(9, after1s.totalRemainingSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `intervalTransition emits on step boundaries`() = runTest {
        val steps = listOf(
            TimerStep(1, IntervalType.WALK, TimerPhase.WARMUP),
            TimerStep(1, IntervalType.WALK, TimerPhase.ACTIVE),
            TimerStep(1, IntervalType.JOG, TimerPhase.ACTIVE),
            TimerStep(1, IntervalType.WALK, TimerPhase.COOLDOWN)
        )

        timer.start(steps, this)

        timer.intervalTransition.test {
            advanceTimeBy(1001) // warmup -> active walk
            assertEquals(IntervalType.WALK, awaitItem())

            advanceTimeBy(1001) // active walk -> active jog
            assertEquals(IntervalType.JOG, awaitItem())

            advanceTimeBy(1001) // active jog -> cooldown walk
            assertEquals(IntervalType.WALK, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }
}
