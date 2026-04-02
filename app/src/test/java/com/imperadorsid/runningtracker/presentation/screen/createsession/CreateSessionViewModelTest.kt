package com.imperadorsid.runningtracker.presentation.screen.createsession

import app.cash.turbine.test
import com.imperadorsid.runningtracker.fake.FakeSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSessionViewModelTest {

    private lateinit var repository: FakeSessionRepository
    private lateinit var viewModel: CreateSessionViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Fixed timestamp: 2026-04-02 12:00:00 UTC
    private val fixedTimestamp = 1775088000000L
    private val fakeClock = { fixedTimestamp }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSessionRepository()
        viewModel = CreateSessionViewModel(repository, fakeClock)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty patterns`() {
        val state = viewModel.uiState.value
        assertEquals(0, state.patterns.size)
        assertEquals(false, state.isSaving)
        assertNull(state.validationError)
        assertNull(state.savedSessionId)
    }

    @Test
    fun `addPattern adds to state`() {
        viewModel.addPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)

        val state = viewModel.uiState.value
        assertEquals(1, state.patterns.size)
        assertEquals(3, state.patterns[0].reps)
        assertEquals(60, state.patterns[0].walkDurationSeconds)
        assertEquals(90, state.patterns[0].jogDurationSeconds)
    }

    @Test
    fun `addPattern multiple times accumulates`() {
        viewModel.addPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
        viewModel.addPattern(reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60)

        assertEquals(2, viewModel.uiState.value.patterns.size)
    }

    @Test
    fun `removePattern removes by index`() {
        viewModel.addPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
        viewModel.addPattern(reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60)

        viewModel.removePattern(0)

        val state = viewModel.uiState.value
        assertEquals(1, state.patterns.size)
        assertEquals(2, state.patterns[0].reps)
    }

    @Test
    fun `saveSession with empty patterns sets validation error`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.saveSession()

            var state = awaitItem()
            while (state.validationError == null) {
                state = awaitItem()
            }
            assertNotNull(state.validationError)
            assertNull(state.savedSessionId)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveSession with patterns saves and emits session id`() = runTest {
        viewModel.addPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)

        viewModel.uiState.test {
            awaitItem() // current state with pattern

            viewModel.saveSession()

            var state = awaitItem()
            while (state.savedSessionId == null) {
                state = awaitItem()
            }
            assertNotNull(state.savedSessionId)
            assertNull(state.validationError)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveSession uses DD-MM date format from clock`() = runTest {
        viewModel.addPattern(reps = 1, walkDurationSeconds = 60, jogDurationSeconds = 60)
        viewModel.saveSession()
        testDispatcher.scheduler.advanceUntilIdle()

        val session = repository.getSessionById(viewModel.uiState.value.savedSessionId!!)!!
        val expectedDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(fixedTimestamp))
        assertEquals(expectedDate, session.dateLabel)
    }
}
