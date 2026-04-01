package com.imperadorsid.runningtracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionWithPatterns>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionWithPatterns?

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Insert
    suspend fun insertPatterns(patterns: List<IntervalPatternEntity>)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}
