package com.imperadorsid.runningtracker.data.local.db

import com.imperadorsid.runningtracker.domain.model.IntervalPattern
import com.imperadorsid.runningtracker.domain.model.Session

fun SessionWithPatterns.toDomain(): Session = Session(
    id = session.id,
    dateLabel = session.dateLabel,
    patterns = patterns.sortedBy { it.orderIndex }.map { it.toDomain() },
    createdAt = session.createdAt
)

fun IntervalPatternEntity.toDomain(): IntervalPattern = IntervalPattern(
    id = id,
    reps = reps,
    walkDurationSeconds = walkDurationSeconds,
    jogDurationSeconds = jogDurationSeconds
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    dateLabel = dateLabel,
    createdAt = createdAt
)

fun Session.toPatternEntities(sessionId: Long): List<IntervalPatternEntity> =
    patterns.mapIndexed { index, pattern ->
        IntervalPatternEntity(
            sessionId = sessionId,
            orderIndex = index,
            reps = pattern.reps,
            walkDurationSeconds = pattern.walkDurationSeconds,
            jogDurationSeconds = pattern.jogDurationSeconds
        )
    }
