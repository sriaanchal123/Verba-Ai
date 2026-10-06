package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

class VerbaNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_PROGRESS_ID = "verba_ingestion_progress"
        const val CHANNEL_COMPLETION_ID = "verba_ingestion_completed"
        const val EXTRA_SESSION_ID = "extra_session_id"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progressChannel = NotificationChannel(
                CHANNEL_PROGRESS_ID,
                "Ingestion & Transcription Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active upload, Whisper transcription, and vector embedding progress"
                setShowBadge(false)
            }

            val completionChannel = NotificationChannel(
                CHANNEL_COMPLETION_ID,
                "Knowledge Extraction Complete",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when documents, meetings, or web URLs have finished processing"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(progressChannel)
            notificationManager.createNotificationChannel(completionChannel)
        }
    }

    fun showProgressNotification(
        notificationId: Int = 1001,
        title: String,
        currentStep: String,
        progressPercent: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PROGRESS_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("VerbaAI: $title")
            .setContentText(currentStep)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progressPercent > 0) {
            builder.setProgress(100, progressPercent, false)
        } else {
            builder.setProgress(100, 0, true)
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled if permission not granted yet
        }
    }

    fun showCompletionNotification(
        notificationId: Int = 1002,
        sessionId: String,
        sessionTitle: String,
        summaryTlDr: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SESSION_ID, sessionId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_COMPLETION_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Knowledge Ready: $sessionTitle")
            .setContentText(summaryTlDr.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(summaryTlDr))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        try {
            // Cancel any running progress notification
            NotificationManagerCompat.from(context).cancel(1001)
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled if permission not granted
        }
    }

    fun cancelNotification(notificationId: Int = 1001) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
