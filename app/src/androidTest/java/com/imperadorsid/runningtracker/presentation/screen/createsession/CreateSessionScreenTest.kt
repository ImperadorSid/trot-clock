package com.imperadorsid.runningtracker.presentation.screen.createsession

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.imperadorsid.runningtracker.presentation.theme.TrotClockTheme
import org.junit.Rule
import org.junit.Test

class CreateSessionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsNewSessionHeader() {
        composeRule.setContent {
            TrotClockTheme {
                CreateSessionScreen(
                    uiState = CreateSessionUiState(),
                    onAddPattern = { _, _, _ -> },
                    onRemovePattern = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("New Session").assertIsDisplayed()
    }

    @Test
    fun showsWalkAndJogLabels() {
        composeRule.setContent {
            TrotClockTheme {
                CreateSessionScreen(
                    uiState = CreateSessionUiState(),
                    onAddPattern = { _, _, _ -> },
                    onRemovePattern = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Walk").assertExists()
        composeRule.onNodeWithText("Jog").assertExists()
    }

    @Test
    fun showsSaveEdgeButton() {
        composeRule.setContent {
            TrotClockTheme {
                CreateSessionScreen(
                    uiState = CreateSessionUiState(),
                    onAddPattern = { _, _, _ -> },
                    onRemovePattern = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("Save").assertExists()
    }

    @Test
    fun showsSavingState() {
        composeRule.setContent {
            TrotClockTheme {
                CreateSessionScreen(
                    uiState = CreateSessionUiState(isSaving = true),
                    onAddPattern = { _, _, _ -> },
                    onRemovePattern = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithText("...").assertExists()
    }
}