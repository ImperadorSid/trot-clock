package com.imperadorsid.runningtracker.service

import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.imperadorsid.runningtracker.domain.model.IntervalType
import com.imperadorsid.runningtracker.domain.model.buildTimerSteps
import com.imperadorsid.runningtracker.domain.repository.SessionRepository
import com.imperadorsid.runningtracker.domain.timer.SessionTimer
import com.imperadorsid.runningtracker.domain.timer.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RunTrackingService : LifecycleService() {

    companion object {
        const val ACTION_START = "com.imperadorsid.runningtracker.ACTION_START"
        const val ACTION_PAUSE = "com.imperadorsid.runningtracker.ACTION_PAUSE"
        const val ACTION_RESUME = "com.imperadorsid.runningtracker.ACTION_RESUME"
        const val ACTION_STOP = "com.imperadorsid.runningtracker.ACTION_STOP"
        const val EXTRA_SESSION_ID = "session_id"

        private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
        val timerState: StateFlow<TimerState> = _timerState

        val intervalTransition: SharedFlow<IntervalType>
            get() = timer.intervalTransition

        private val timer = SessionTimer()
        private var repository: SessionRepository? = null

        fun setRepository(repository: SessionRepository) {
            this.repository = repository
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
            timer.timerState.collect { state ->
                _timerState.value = state
                when (state) {
                    is TimerState.Running -> {
                        val stepChanged = state.currentStepIndex != lastNotificationStep
                        val pauseChanged = state.isPaused != lastNotificationPaused
                        if (stepChanged || pauseChanged) {
                            lastNotificationStep = state.currentStepIndex
                            lastNotificationPaused = state.isPaused
                            val text = formatPhaseLabel(state)
                            val builder = notificationHelper.buildNotificationBuilder(text, state.isPaused)
                            val manager = getSystemService(NotificationManager::class.java)
                            manager.notify(NotificationHelper.NOTIFICATION_ID, builder.build())
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

    private fun formatPhaseLabel(state: TimerState.Running): String {
        return when (state.currentStep.phase) {
            com.imperadorsid.runningtracker.domain.model.TimerPhase.WARMUP -> "Warmup"
            com.imperadorsid.runningtracker.domain.model.TimerPhase.ACTIVE -> state.currentStep.type.name
            com.imperadorsid.runningtracker.domain.model.TimerPhase.COOLDOWN -> "Cooldown"
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1)
                if (sessionId != -1L) {
                    startSession(sessionId)
                }
            }
            ACTION_PAUSE -> timer.pause()
            ACTION_RESUME -> timer.resume()
            ACTION_STOP -> {
                timer.stop()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startSession(sessionId: Long) {
        val repo = repository ?: return

        lifecycleScope.launch {
            val session = repo.getSessionById(sessionId) ?: return@launch
            val steps = buildTimerSteps(session.patterns)

            val builder = notificationHelper.buildNotificationBuilder("Starting...", isPaused = false)
            startForeground(NotificationHelper.NOTIFICATION_ID, builder.build())
            ongoingActivityManager.create(NotificationHelper.NOTIFICATION_ID, builder)

            timer.start(steps, lifecycleScope)
        }
    }

    override fun onDestroy() {
        timer.stop()
        super.onDestroy()
    }
}
