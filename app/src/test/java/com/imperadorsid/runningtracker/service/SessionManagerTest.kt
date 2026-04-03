package com.imperadorsid.runningtracker.service

import app.cash.turbine.test
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.timer.SessionTimer
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.fake.FakeSessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private lateinit var manager: SessionManager
    private lateinit var repository: FakeSessionRepository

    @Before
    fun setup() {
        repository = FakeSessionRepository()
        manager = SessionManager(
            timer = SessionTimer(),
            repository = repository
        )
    }

    private suspend fun insertTestSession(intervalsOnly: Boolean = false): Long {
        return repository.insertSession(
            Session(
                dateLabel = "01/04",
                patterns = listOf(
                    IntervalPattern(reps = 1, walkDurationSeconds = 2, jogDurationSeconds = 2)
                ),
                createdAt = 1000L
            )
        )
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(TimerState.Idle, manager.timerState.value)
    }

    @Test
    fun `handleAction START with valid session starts timer`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            assertEquals(TimerState.Idle, awaitItem())

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceUntilIdle()

            val state = awaitItem() as TimerState.Running
            assertEquals(0, state.currentStepIndex)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `handleAction START with invalid session id does nothing`() = runTest {
        manager.handleAction(SessionManager.ACTION_START, -1L, false, this)
        advanceUntilIdle()
        assertEquals(TimerState.Idle, manager.timerState.value)
    }

    @Test
    fun `handleAction START with non-existent session does nothing`() = runTest {
        manager.handleAction(SessionManager.ACTION_START, 999L, false, this)
        advanceUntilIdle()
        assertEquals(TimerState.Idle, manager.timerState.value)
    }

    @Test
    fun `handleAction PAUSE pauses running timer`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            // Only advance enough to start the timer, not complete it
            advanceTimeBy(1)
            val running = awaitItem()
            assertTrue(running is TimerState.Running)

            manager.handleAction(SessionManager.ACTION_PAUSE, scope = this@runTest)
            var state = awaitItem()
            while (state is TimerState.Running && !state.isPaused) {
                state = awaitItem()
            }
            assertTrue(state is TimerState.Running)
            assertTrue((state as TimerState.Running).isPaused)

            manager.stop()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `handleAction RESUME resumes paused timer`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceTimeBy(1)
            awaitItem() // Running

            manager.handleAction(SessionManager.ACTION_PAUSE, scope = this@runTest)
            var state = awaitItem()
            while (state is TimerState.Running && !state.isPaused) {
                state = awaitItem()
            }

            manager.handleAction(SessionManager.ACTION_RESUME, scope = this@runTest)
            val resumed = awaitItem() as TimerState.Running
            assertFalse(resumed.isPaused)

            manager.stop()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `handleAction STOP resets to Idle`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceTimeBy(1)
            awaitItem() // Running

            manager.handleAction(SessionManager.ACTION_STOP, scope = this@runTest)
            var state: TimerState = awaitItem()
            while (state !is TimerState.Idle) {
                state = awaitItem()
            }
            assertEquals(TimerState.Idle, state)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `stop resets timer to Idle`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceTimeBy(1)
            awaitItem() // Running

            manager.stop()
            var state: TimerState = awaitItem()
            while (state !is TimerState.Idle) {
                state = awaitItem()
            }
            assertEquals(TimerState.Idle, state)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `handleAction START with intervalsOnly skips warmup and cooldown`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, true, this@runTest)
            advanceUntilIdle()

            val state = awaitItem() as TimerState.Running
            // With intervalsOnly=true, first step should be ACTIVE (not WARMUP)
            assertEquals(com.imperadorsid.runningtracker.domain.model.TimerPhase.ACTIVE, state.currentStep.phase)
            // Total remaining should be 4s (2s walk + 2s jog, no warmup/cooldown)
            assertEquals(4, state.totalRemainingSeconds)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `handleAction START without intervalsOnly includes warmup`() = runTest {
        val sessionId = insertTestSession()

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceUntilIdle()

            val state = awaitItem() as TimerState.Running
            assertEquals(com.imperadorsid.runningtracker.domain.model.TimerPhase.WARMUP, state.currentStep.phase)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `shouldUpdateNotification returns true when step changes`() {
        val step = com.imperadorsid.runningtracker.domain.model.TimerStep(
            60, com.imperadorsid.runningtracker.domain.model.IntervalType.WALK,
            com.imperadorsid.runningtracker.domain.model.TimerPhase.ACTIVE
        )
        val state = TimerState.Running(1, step, 30, 60)
        assertTrue(manager.shouldUpdateNotification(state, 0, false))
    }

    @Test
    fun `shouldUpdateNotification returns true when pause state changes`() {
        val step = com.imperadorsid.runningtracker.domain.model.TimerStep(
            60, com.imperadorsid.runningtracker.domain.model.IntervalType.WALK,
            com.imperadorsid.runningtracker.domain.model.TimerPhase.ACTIVE
        )
        val state = TimerState.Running(0, step, 30, 60, isPaused = true)
        assertTrue(manager.shouldUpdateNotification(state, 0, false))
    }

    @Test
    fun `shouldUpdateNotification returns false when nothing changes`() {
        val step = com.imperadorsid.runningtracker.domain.model.TimerStep(
            60, com.imperadorsid.runningtracker.domain.model.IntervalType.WALK,
            com.imperadorsid.runningtracker.domain.model.TimerPhase.ACTIVE
        )
        val state = TimerState.Running(0, step, 30, 60)
        assertFalse(manager.shouldUpdateNotification(state, 0, false))
    }

    @Test
    fun `handleAction with null repository does nothing`() = runTest {
        val managerNoRepo = SessionManager(timer = SessionTimer())
        managerNoRepo.handleAction(SessionManager.ACTION_START, 1L, false, this)
        advanceUntilIdle()
        assertEquals(TimerState.Idle, managerNoRepo.timerState.value)
    }

    @Test
    fun `setRepository allows subsequent starts`() = runTest {
        val managerLate = SessionManager(timer = SessionTimer())
        managerLate.setRepository(repository)
        val sessionId = insertTestSession()

        managerLate.timerState.test {
            awaitItem() // Idle

            managerLate.handleAction(SessionManager.ACTION_START, sessionId, false, this@runTest)
            advanceUntilIdle()

            val state = awaitItem() as TimerState.Running
            assertEquals(0, state.currentStepIndex)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `timer completes after all steps finish`() = runTest {
        val sessionId = repository.insertSession(
            Session(
                dateLabel = "01/04",
                patterns = listOf(
                    IntervalPattern(reps = 1, walkDurationSeconds = 1, jogDurationSeconds = 1)
                ),
                createdAt = 1000L
            )
        )

        manager.timerState.test {
            awaitItem() // Idle

            manager.handleAction(SessionManager.ACTION_START, sessionId, true, this@runTest)
            advanceUntilIdle()
            awaitItem() // Running

            // Advance past all steps (1s walk + 1s jog = 2s, intervalsOnly)
            advanceTimeBy(3001)
            var state: TimerState = awaitItem()
            while (state !is TimerState.Completed) {
                state = awaitItem()
            }
            assertEquals(TimerState.Completed, state)

            cancelAndConsumeRemainingEvents()
        }
    }
}
