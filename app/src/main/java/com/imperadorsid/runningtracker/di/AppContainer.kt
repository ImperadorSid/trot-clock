package com.imperadorsid.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.imperadorsid.runningtracker.data.local.db.RunningTrackerDatabase
import com.imperadorsid.runningtracker.data.repository.SessionRepositoryImpl
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.util.Clock
import com.imperadorsid.runningtracker.domain.util.SystemClock

class AppContainer(context: Context) {

    private val database: RunningTrackerDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            RunningTrackerDatabase::class.java,
            "running_tracker.db"
        ).build()
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(database.sessionDao())
    }

    val clock: Clock = SystemClock
}
