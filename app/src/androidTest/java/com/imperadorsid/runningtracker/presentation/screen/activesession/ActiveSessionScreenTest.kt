package com.imperadorsid.runningtracker.presentation.screen.activesession

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.model.TimerStep
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.presentation.theme.TrotClockTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActiveSessionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val testSession = Session(
        id = 1, dateLabel = "01/04",
        patterns = listOf(IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 60)),
        createdAt = 1000L
    )

    @Test
    fun idleStateShowsSessionInfoAndStartButton() {
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, TimerState.Idle),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("01/04").assertIsDisplayed()
        composeRule.onNodeWithText("Start").assertIsDisplayed()
    }

    @Test
    fun startButtonCallsOnStart() {
        var startedId = -1L
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, TimerState.Idle),
                    onStart = { id, _ -> startedId = id }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Start").performClick()
        assertEquals(1L, startedId)
    }

    @Test
    fun runningStateShowsWalkLabel() {
        val timerState = TimerState.Running(
            currentStepIndex = 1,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 45,
            totalRemainingSeconds = 345
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("WALK").assertIsDisplayed()
        composeRule.onNodeWithText("0:45").assertIsDisplayed()
    }

    @Test
    fun runningStateShowsJogLabel() {
        val timerState = TimerState.Running(
            currentStepIndex = 2,
            currentStep = TimerStep(90, IntervalType.JOG, TimerPhase.ACTIVE),
            remainingSeconds = 30,
            totalRemainingSeconds = 200
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("JOG").assertIsDisplayed()
        composeRule.onNodeWithText("0:30").assertIsDisplayed()
    }

    @Test
    fun warmupPhaseShowsWarmupLabel() {
        val timerState = TimerState.Running(
            currentStepIndex = 0,
            currentStep = TimerStep(600, IntervalType.WALK, TimerPhase.WARMUP),
            remainingSeconds = 550,
            totalRemainingSeconds = 900
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("WARMUP").assertIsDisplayed()
    }

    @Test
    fun pausedStateShowsPausedLabelAndResumeButton() {
        val timerState = TimerState.Running(
            currentStepIndex = 1,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 30,
            totalRemainingSeconds = 200,
            isPaused = true
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("PAUSED").assertIsDisplayed()
        composeRule.onNodeWithText("Resume").assertIsDisplayed()
    }

    @Test
    fun pauseButtonCallsOnPause() {
        var pauseCalled = false
        val timerState = TimerState.Running(
            currentStepIndex = 1,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 30,
            totalRemainingSeconds = 200
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = { pauseCalled = true }, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Pause").performClick()
        assertEquals(true, pauseCalled)
    }

    @Test
    fun stopButtonCallsOnStop() {
        var stopCalled = false
        val timerState = TimerState.Running(
            currentStepIndex = 1,
            currentStep = TimerStep(60, IntervalType.WALK, TimerPhase.ACTIVE),
            remainingSeconds = 30,
            totalRemainingSeconds = 200
        )
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, timerState),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = { stopCalled = true }, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Stop").performClick()
        assertEquals(true, stopCalled)
    }

    @Test
    fun completedStateShowsDoneAndBackButton() {
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Ready(testSession, TimerState.Completed),
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Done!").assertIsDisplayed()
        composeRule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsMessage() {
        composeRule.setContent {
            TrotClockTheme {
                ActiveSessionScreen(
                    uiState = ActiveSessionUiState.Error,
                    onStart = { _, _ -> }, onPause = {}, onResume = {}, onStop = {}, onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Session not found").assertIsDisplayed()
    }
}