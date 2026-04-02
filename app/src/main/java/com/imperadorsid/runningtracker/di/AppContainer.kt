package com.imperadorsid.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.imperadorsid.runningtracker.data.local.db.TrotClockDatabase
import com.imperadorsid.runningtracker.data.repository.SessionRepositoryImpl
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.util.Clock
import com.imperadorsid.runningtracker.domain.util.SystemClock

class AppContainer(context: Context) {

    private val database: TrotClockDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            TrotClockDatabase::class.java,
            "trotclock.db"
        ).build()
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(database.sessionDao())
    }

    val clock: Clock = SystemClock
}
