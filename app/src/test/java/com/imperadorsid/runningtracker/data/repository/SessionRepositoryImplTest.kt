package com.imperadorsid.runningtracker.data.repository

import app.cash.turbine.test
import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.fake.FakeSessionDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionRepositoryImplTest {

    private lateinit var dao: FakeSessionDao
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setup() {
        dao = FakeSessionDao()
        repository = SessionRepositoryImpl(dao)
    }

    @Test
    fun `getSessions emits empty list initially`() = runTest {
        repository.getSessions().test {
            assertEquals(emptyList<Session>(), awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `insertSession and getSessions emits updated list`() = runTest {
        repository.getSessions().test {
            assertEquals(0, awaitItem().size)

            val session = Session(
                dateLabel = "01/04",
                patterns = listOf(
                    IntervalPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
                ),
                createdAt = 1000L
            )
            repository.insertSession(session)

            // Skip intermediate emissions until we get the session with patterns
            var updated = awaitItem()
            while (updated.isEmpty() || updated[0].patterns.isEmpty()) {
                updated = awaitItem()
            }
            assertEquals(1, updated.size)
            assertEquals("01/04", updated[0].dateLabel)
            assertEquals(1, updated[0].patterns.size)
            assertEquals(3, updated[0].patterns[0].reps)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getSessionById returns session with patterns`() = runTest {
        val session = Session(
            dateLabel = "01/04",
            patterns = listOf(
                IntervalPattern(reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
            ),
            createdAt = 1000L
        )
        val id = repository.insertSession(session)

        val result = repository.getSessionById(id)!!
        assertEquals("01/04", result.dateLabel)
        assertEquals(1, result.patterns.size)
    }

    @Test
    fun `getSessionById returns null for missing id`() = runTest {
        assertNull(repository.getSessionById(999L))
    }

    @Test
    fun `deleteSession removes session from flow`() = runTest {
        repository.getSessions().test {
            awaitItem() // initial empty

            val id = repository.insertSession(
                Session(dateLabel = "01/04", patterns = emptyList(), createdAt = 1000L)
            )
            assertEquals(1, awaitItem().size)

            repository.deleteSession(id)
            assertEquals(0, awaitItem().size)

            cancelAndConsumeRemainingEvents()
        }
    }
}
