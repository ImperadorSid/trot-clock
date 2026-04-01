package com.imperadorsid.runningtracker.fake

import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionRepository : SessionRepository {

    private var nextId = 1L
    private val sessions = MutableStateFlow<List<Session>>(emptyList())

    override fun getSessions(): Flow<List<Session>> =
        sessions.map { list -> list.sortedByDescending { session -> session.createdAt } }

    override suspend fun getSessionById(id: Long): Session? =
        sessions.value.find { session -> session.id == id }

    override suspend fun insertSession(session: Session): Long {
        val id = nextId++
        val withId = session.copy(id = id)
        sessions.value = sessions.value + withId
        return id
    }

    override suspend fun deleteSession(id: Long) {
        sessions.value = sessions.value.filter { session -> session.id != id }
    }
}
