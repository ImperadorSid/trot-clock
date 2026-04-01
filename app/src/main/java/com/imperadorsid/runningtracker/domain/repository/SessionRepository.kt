package com.imperadorsid.runningtracker.domain.repository

import com.imperadorsid.runningtracker.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: Long): Session?
    suspend fun insertSession(session: Session): Long
    suspend fun deleteSession(id: Long)
}
