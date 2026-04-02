package com.imperadorsid.runningtracker.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.LocusIdCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.imperadorsid.runningtracker.R
import com.imperadorsid.runningtracker.domain.model.TimerPhase
import com.imperadorsid.runningtracker.domain.timer.TimerState
import com.imperadorsid.runningtracker.presentation.MainActivity

class OngoingActivityManager(private val context: Context) {

    private val tapPending: PendingIntent by lazy {
        val tapIntent = Intent(context, MainActivity::class.java)
        PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun create(notificationId: Int, notificationBuilder: NotificationCompat.Builder) {
        val ongoingActivity = OngoingActivity.Builder(context, notificationId, notificationBuilder)
            .setStaticIcon(R.mipmap.ic_launcher)
            .setTouchIntent(tapPending)
            .setLocusId(LocusIdCompat("running_session"))
            .setStatus(
                Status.Builder()
                    .addTemplate("Starting...")
                    .build()
            )
            .build()

        ongoingActivity.apply(context)
    }

    fun update(state: TimerState.Running, notificationBuilder: NotificationCompat.Builder) {
        val phaseLabel = when (state.currentStep.phase) {
            TimerPhase.WARMUP -> "Warmup"
            TimerPhase.ACTIVE -> state.currentStep.type.name
            TimerPhase.COOLDOWN -> "Cooldown"
        }

        val ongoingActivity = OngoingActivity.Builder(
            context,
            NotificationHelper.NOTIFICATION_ID,
            notificationBuilder
        )
            .setStaticIcon(R.mipmap.ic_launcher)
            .setTouchIntent(tapPending)
            .setStatus(
                Status.Builder()
                    .addTemplate(phaseLabel)
                    .build()
            )
            .build()

        ongoingActivity.apply(context)
    }
}
