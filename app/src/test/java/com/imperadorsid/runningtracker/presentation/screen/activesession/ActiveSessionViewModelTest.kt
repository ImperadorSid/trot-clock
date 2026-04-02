package com.imperadorsid.runningtracker.presentation.screen.activesession

import app.cash.turbine.test
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.fake.FakeSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private lateinit var repository: FakeSessionRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSessionRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val id = repository.insertSession(
            Session(dateLabel = "01/04", patterns = listOf(
                IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 60)
            ), createdAt = 1000L)
        )
        val viewModel = ActiveSessionViewModel(id, repository)

        assertTrue(viewModel.uiState.value is ActiveSessionUiState.Loading)
    }

    @Test
    fun `loads session and transitions to Ready`() = runTest {
        val id = repository.insertSession(
            Session(dateLabel = "01/04", patterns = listOf(
                IntervalPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 60)
            ), createdAt = 1000L)
        )
        val viewModel = ActiveSessionViewModel(id, repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is ActiveSessionUiState.Loading) {
                state = awaitItem()
            }
            assertTrue(state is ActiveSessionUiState.Ready)
            val ready = state as ActiveSessionUiState.Ready
            assertEquals("01/04", ready.session.dateLabel)
            assertEquals(TimerState.Idle, ready.timerState)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `invalid session id shows Error`() = runTest {
        val viewModel = ActiveSessionViewModel(999L, repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is ActiveSessionUiState.Loading) {
                state = awaitItem()
            }
            assertTrue(state is ActiveSessionUiState.Error)

            cancelAndConsumeRemainingEvents()
        }
    }
}