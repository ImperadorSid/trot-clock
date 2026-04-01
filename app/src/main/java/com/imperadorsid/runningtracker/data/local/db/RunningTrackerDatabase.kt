package com.imperadorsid.runningtracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SessionEntity::class, IntervalPatternEntity::class],
    version = 1
)
abstract class RunningTrackerDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
