package com.imperadorsid.runningtracker.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var database: RunningTrackerDatabase
    private lateinit var dao: SessionDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunningTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.sessionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertSession_returnsGeneratedId() = runTest {
        val id = dao.insertSession(SessionEntity(dateLabel = "01/04", createdAt = 1000L))
        assertEquals(1L, id)
    }

    @Test
    fun getAllSessions_returnsSessionsWithPatternsOrderedByCreatedAtDesc() = runTest {
        val id1 = dao.insertSession(SessionEntity(dateLabel = "01/04", createdAt = 1000L))
        dao.insertPatterns(listOf(
            IntervalPatternEntity(sessionId = id1, orderIndex = 0, reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
        ))

        val id2 = dao.insertSession(SessionEntity(dateLabel = "02/04", createdAt = 2000L))
        dao.insertPatterns(listOf(
            IntervalPatternEntity(sessionId = id2, orderIndex = 0, reps = 5, walkDurationSeconds = 120, jogDurationSeconds = 60)
        ))

        val sessions = dao.getAllSessions().first()

        assertEquals(2, sessions.size)
        assertEquals("02/04", sessions[0].session.dateLabel)
        assertEquals("01/04", sessions[1].session.dateLabel)
        assertEquals(1, sessions[0].patterns.size)
        assertEquals(5, sessions[0].patterns[0].reps)
    }

    @Test
    fun getSessionById_returnsSessionWithPatterns() = runTest {
        val id = dao.insertSession(SessionEntity(dateLabel = "01/04", createdAt = 1000L))
        dao.insertPatterns(listOf(
            IntervalPatternEntity(sessionId = id, orderIndex = 0, reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90),
            IntervalPatternEntity(sessionId = id, orderIndex = 1, reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60)
        ))

        val result = dao.getSessionById(id)!!

        assertEquals("01/04", result.session.dateLabel)
        assertEquals(2, result.patterns.size)
    }

    @Test
    fun getSessionById_returnsNullForMissingId() = runTest {
        assertNull(dao.getSessionById(999L))
    }

    @Test
    fun deleteSession_cascadesDeleteToPatterns() = runTest {
        val id = dao.insertSession(SessionEntity(dateLabel = "01/04", createdAt = 1000L))
        dao.insertPatterns(listOf(
            IntervalPatternEntity(sessionId = id, orderIndex = 0, reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90)
        ))

        dao.deleteSession(id)

        val sessions = dao.getAllSessions().first()
        assertEquals(0, sessions.size)
        assertNull(dao.getSessionById(id))
    }

    @Test
    fun patternsOrderedByOrderIndex() = runTest {
        val id = dao.insertSession(SessionEntity(dateLabel = "01/04", createdAt = 1000L))
        dao.insertPatterns(listOf(
            IntervalPatternEntity(sessionId = id, orderIndex = 2, reps = 1, walkDurationSeconds = 30, jogDurationSeconds = 30),
            IntervalPatternEntity(sessionId = id, orderIndex = 0, reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90),
            IntervalPatternEntity(sessionId = id, orderIndex = 1, reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60)
        ))

        val result = dao.getSessionById(id)!!
        val ordered = result.patterns.sortedBy { it.orderIndex }

        assertEquals(3, ordered[0].reps)
        assertEquals(2, ordered[1].reps)
        assertEquals(1, ordered[2].reps)
    }
}
