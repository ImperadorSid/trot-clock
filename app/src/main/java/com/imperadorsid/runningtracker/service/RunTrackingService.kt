package com.imperadorsid.runningtracker.service

import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.presentation.util.formatPhaseLabel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunTrackingService : LifecycleService() {

    companion object {
        const val ACTION_START = SessionManager.ACTION_START
        const val ACTION_PAUSE = SessionManager.ACTION_PAUSE
        const val ACTION_RESUME = SessionManager.ACTION_RESUME
        const val ACTION_STOP = SessionManager.ACTION_STOP
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_INTERVALS_ONLY = "intervals_only"

        private val manager = SessionManager()

        val timerState: StateFlow<TimerState> = manager.timerState

        val intervalTransition: SharedFlow<IntervalType> = manager.intervalTransition

        fun setRepository(repository: SessionRepository) {
            manager.setRepository(repository)
        }
    }

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var ongoingActivityManager: OngoingActivityManager

    private var lastNotificationStep: Int? = null
    private var lastNotificationPaused: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        ongoingActivityManager = OngoingActivityManager(this)
        notificationHelper.createChannel()

        lifecycleScope.launch {
            manager.timerState.collect { state ->
                when (state) {
                    is TimerState.Running -> {
                        if (manager.shouldUpdateNotification(state, lastNotificationStep, lastNotificationPaused)) {
                            lastNotificationStep = state.currentStepIndex
                            lastNotificationPaused = state.isPaused
                            val text = formatPhaseLabel(state)
                            val builder = notificationHelper.buildNotificationBuilder(text, state.isPaused)
                            val notificationManager = getSystemService(NotificationManager::class.java)
                            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, builder.build())
                            ongoingActivityManager.update(state, builder)
                        }
                    }
                    is TimerState.Completed -> {
                        stopSelf()
                    }
                    is TimerState.Idle -> {}
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val action = intent?.action
        if (action != null) {
            val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)
            val intervalsOnly = intent.getBooleanExtra(EXTRA_INTERVALS_ONLY, false)

            if (action == ACTION_START && sessionId != -1L) {
                val builder = notificationHelper.buildNotificationBuilder("Starting...", isPaused = false)
                startForeground(NotificationHelper.NOTIFICATION_ID, builder.build())
                ongoingActivityManager.create(NotificationHelper.NOTIFICATION_ID, builder)
            }

            manager.handleAction(action, sessionId, intervalsOnly, lifecycleScope)

            if (action == ACTION_STOP) {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        manager.stop()
        super.onDestroy()
    }
}
