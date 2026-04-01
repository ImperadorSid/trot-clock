package com.imperadorsid.runningtracker.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateLabel: String,
    val createdAt: Long
)
