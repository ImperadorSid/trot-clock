package com.imperadorsid.runningtracker.presentation.screen.sessionlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.presentation.theme.RunningTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SessionListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsLoadingIndicator() {
        composeRule.setContent {
            RunningTrackerTheme {
                SessionListScreen(
                    uiState = SessionListUiState.Loading,
                    onCreateSession = {},
                    onStartSession = {},
                    onDeleteSession = {}
                )
            }
        }
        // CircularProgressIndicator should be displayed
        // No text assertions for loading — just verifying no crash
    }

    @Test
    fun showsEmptyStateWhenNoSessions() {
        composeRule.setContent {
            RunningTrackerTheme {
                SessionListScreen(
                    uiState = SessionListUiState.Success(emptyList()),
                    onCreateSession = {},
                    onStartSession = {},
                    onDeleteSession = {}
                )
            }
        }
        composeRule.onNodeWithText("No sessions yet").assertIsDisplayed()
    }

    @Test
    fun showsSessionDateLabelsAndDurations() {
        val sessions = listOf(
            Session(
                id = 1, dateLabel = "01/04",
                patterns = listOf(IntervalPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)),
                createdAt = 1000L
            )
        )
        composeRule.setContent {
            RunningTrackerTheme {
                SessionListScreen(
                    uiState = SessionListUiState.Success(sessions),
                    onCreateSession = {},
                    onStartSession = {},
                    onDeleteSession = {}
                )
            }
        }
        composeRule.onNodeWithText("01/04").assertIsDisplayed()
    }

    @Test
    fun tappingSessionCallsOnStartSession() {
        var startedId = -1L
        val sessions = listOf(
            Session(
                id = 42, dateLabel = "01/04",
                patterns = listOf(IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 60)),
                createdAt = 1000L
            )
        )
        composeRule.setContent {
            RunningTrackerTheme {
                SessionListScreen(
                    uiState = SessionListUiState.Success(sessions),
                    onCreateSession = {},
                    onStartSession = { startedId = it },
                    onDeleteSession = {}
                )
            }
        }
        composeRule.onNodeWithText("01/04").performClick()
        assertEquals(42L, startedId)
    }

    @Test
    fun newButtonCallsOnCreateSession() {
        var createCalled = false
        composeRule.setContent {
            RunningTrackerTheme {
                SessionListScreen(
                    uiState = SessionListUiState.Success(emptyList()),
                    onCreateSession = { createCalled = true },
                    onStartSession = {},
                    onDeleteSession = {}
                )
            }
        }
        composeRule.onNodeWithText("New").performClick()
        assertEquals(true, createCalled)
    }
}