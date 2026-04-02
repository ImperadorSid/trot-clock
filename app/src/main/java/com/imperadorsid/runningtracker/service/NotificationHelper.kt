package com.imperadorsid.runningtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.imperadorsid.runningtracker.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "run_tracking_channel"
        const val NOTIFICATION_ID = 1
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun buildNotificationBuilder(
        contentText: String,
        isPaused: Boolean
    ): NotificationCompat.Builder {
        val pauseResumeIntent = Intent(context, RunTrackingService::class.java).apply {
            action = if (isPaused) RunTrackingService.ACTION_RESUME else RunTrackingService.ACTION_PAUSE
        }
        val pauseResumePending = PendingIntent.getService(
            context, 0, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, RunTrackingService::class.java).apply {
            action = RunTrackingService.ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            context, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeLabel = if (isPaused) {
            context.getString(R.string.resume)
        } else {
            context.getString(R.string.pause)
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(contentText)
            .setOngoing(true)
            .setSilent(true)
            .addAction(0, pauseResumeLabel, pauseResumePending)
            .addAction(0, context.getString(R.string.stop), stopPending)
    }

    fun buildNotification(
        contentText: String,
        isPaused: Boolean
    ): Notification {
        return buildNotificationBuilder(contentText, isPaused).build()
    }
}
