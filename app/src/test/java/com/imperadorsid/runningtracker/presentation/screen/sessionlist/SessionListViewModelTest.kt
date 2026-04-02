package com.imperadorsid.runningtracker.presentation.screen.sessionlist

import app.cash.turbine.test
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
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
class SessionListViewModelTest {

    private lateinit var repository: FakeSessionRepository
    private lateinit var viewModel: SessionListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSessionRepository()
        viewModel = SessionListViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then emits Success`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem() is SessionListUiState.Loading)

            val success = awaitItem() as SessionListUiState.Success
            assertEquals(0, success.sessions.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `emits sessions from repository`() = runTest {
        repository.insertSession(
            Session(dateLabel = "01/04", patterns = listOf(
                IntervalPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
            ), createdAt = 1000L)
        )
        viewModel = SessionListViewModel(repository)

        viewModel.uiState.test {
            // Skip Loading
            var state = awaitItem()
            while (state is SessionListUiState.Loading) {
                state = awaitItem()
            }
            val success = state as SessionListUiState.Success
            assertEquals(1, success.sessions.size)
            assertEquals("01/04", success.sessions[0].dateLabel)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteSession removes session from list`() = runTest {
        val id = repository.insertSession(
            Session(dateLabel = "01/04", patterns = emptyList(), createdAt = 1000L)
        )
        viewModel = SessionListViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is SessionListUiState.Loading ||
                (state is SessionListUiState.Success && state.sessions.isEmpty())) {
                state = awaitItem()
            }
            assertEquals(1, (state as SessionListUiState.Success).sessions.size)

            viewModel.deleteSession(id)

            var updated = awaitItem()
            while (updated is SessionListUiState.Success && updated.sessions.isNotEmpty()) {
                updated = awaitItem()
            }
            assertEquals(0, (updated as SessionListUiState.Success).sessions.size)

            cancelAndConsumeRemainingEvents()
        }
    }
}