package com.imperadorsid.runningtracker.fake

import com.imperadorsid.runningtracker.data.local.db.IntervalPatternEntity
import com.imperadorsid.runningtracker.data.local.db.SessionDao
import com.imperadorsid.runningtracker.data.local.db.SessionEntity
import com.imperadorsid.runningtracker.data.local.db.SessionWithPatterns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class FakeSessionDao : SessionDao {

    private var nextSessionId = 1L
    private var nextPatternId = 1L
    private val sessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    private val patterns = MutableStateFlow<List<IntervalPatternEntity>>(emptyList())

    override fun getAllSessions(): Flow<List<SessionWithPatterns>> =
        sessions.combine(patterns) { sessionList, patternList ->
            sessionList
                .sortedByDescending { it.createdAt }
                .map { session ->
                    SessionWithPatterns(
                        session = session,
                        patterns = patternList.filter { it.sessionId == session.id }
                    )
                }
        }

    override suspend fun getSessionById(id: Long): SessionWithPatterns? {
        val session = sessions.value.find { it.id == id } ?: return null
        return SessionWithPatterns(
            session = session,
            patterns = patterns.value.filter { it.sessionId == id }
        )
    }

    override suspend fun insertSession(session: SessionEntity): Long {
        val id = nextSessionId++
        val withId = session.copy(id = id)
        sessions.value = sessions.value + withId
        return id
    }

    override suspend fun insertPatterns(patterns: List<IntervalPatternEntity>) {
        val withIds = patterns.map { it.copy(id = nextPatternId++) }
        this.patterns.value = this.patterns.value + withIds
    }

    override suspend fun deleteSession(id: Long) {
        sessions.value = sessions.value.filter { it.id != id }
        patterns.value = patterns.value.filter { it.sessionId != id }
    }
}
