package com.imperadorsid.runningtracker.data.local.db

import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionMapperTest {

    @Test
    fun `SessionWithPatterns toDomain maps correctly`() {
        val sessionWithPatterns = SessionWithPatterns(
            session = SessionEntity(id = 1, dateLabel = "01/04", createdAt = 1000L),
            patterns = listOf(
                IntervalPatternEntity(
                    id = 10, sessionId = 1, orderIndex = 0,
                    reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90
                ),
                IntervalPatternEntity(
                    id = 11, sessionId = 1, orderIndex = 1,
                    reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60
                )
            )
        )

        val session = sessionWithPatterns.toDomain()

        assertEquals(1L, session.id)
        assertEquals("01/04", session.dateLabel)
        assertEquals(1000L, session.createdAt)
        assertEquals(2, session.patterns.size)
        assertEquals(IntervalPattern(id = 10, reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90), session.patterns[0])
        assertEquals(IntervalPattern(id = 11, reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60), session.patterns[1])
    }

    @Test
    fun `SessionWithPatterns toDomain orders patterns by orderIndex`() {
        val sessionWithPatterns = SessionWithPatterns(
            session = SessionEntity(id = 1, dateLabel = "01/04", createdAt = 1000L),
            patterns = listOf(
                IntervalPatternEntity(
                    id = 11, sessionId = 1, orderIndex = 1,
                    reps = 2, walkDurationSeconds = 120, jogDurationSeconds = 60
                ),
                IntervalPatternEntity(
                    id = 10, sessionId = 1, orderIndex = 0,
                    reps = 3, walkDurationSeconds = 60, jogDurationSeconds = 90
                )
            )
        )

        val session = sessionWithPatterns.toDomain()

        assertEquals(3, session.patterns[0].reps)
        assertEquals(2, session.patterns[1].reps)
    }

    @Test
    fun `Session toEntity maps correctly`() {
        val session = Session(
            id = 5,
            dateLabel = "15/03",
            patterns = listOf(
                IntervalPattern(reps = 4, walkDurationSeconds = 60, jogDurationSeconds = 120)
            ),
            createdAt = 2000L
        )

        val entity = session.toEntity()

        assertEquals(5L, entity.id)
        assertEquals("15/03", entity.dateLabel)
        assertEquals(2000L, entity.createdAt)
    }

    @Test
    fun `Session toPatternEntities maps correctly with sessionId and orderIndex`() {
        val session = Session(
            id = 0,
            dateLabel = "15/03",
            patterns = listOf(
                IntervalPattern(reps = 4, walkDurationSeconds = 60, jogDurationSeconds = 120),
                IntervalPattern(reps = 2, walkDurationSeconds = 30, jogDurationSeconds = 90)
            ),
            createdAt = 2000L
        )

        val patternEntities = session.toPatternEntities(sessionId = 7)

        assertEquals(2, patternEntities.size)
        assertEquals(7L, patternEntities[0].sessionId)
        assertEquals(0, patternEntities[0].orderIndex)
        assertEquals(4, patternEntities[0].reps)
        assertEquals(7L, patternEntities[1].sessionId)
        assertEquals(1, patternEntities[1].orderIndex)
        assertEquals(2, patternEntities[1].reps)
    }
}
