package com.imperadorsid.runningtracker.data.local.db

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithPatterns(
    @Embedded val session: SessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val patterns: List<IntervalPatternEntity>
)
