package com.imperadorsid.runningtracker.data.repository

import com.imperadorsid.runningtracker.data.local.db.SessionDao
import com.imperadorsid.runningtracker.data.local.db.toDomain
import com.imperadorsid.runningtracker.data.local.db.toEntity
import com.imperadorsid.runningtracker.data.local.db.toPatternEntities
import com.imperadorsid.runningtracker.domain.model.Session
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(private val dao: SessionDao) : SessionRepository {

    override fun getSessions(): Flow<List<Session>> =
        dao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionById(id: Long): Session? =
        dao.getSessionById(id)?.toDomain()

    override suspend fun insertSession(session: Session): Long {
        val sessionId = dao.insertSession(session.toEntity())
        dao.insertPatterns(session.toPatternEntities(sessionId))
        return sessionId
    }

    override suspend fun deleteSession(id: Long) {
        dao.deleteSession(id)
    }
}
