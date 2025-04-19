package com.example.safesound.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.safesound.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        const val CHANNEL_ID = "safesound_notifications"
        const val CHANNEL_NAME = "SafeSound Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for recording status and chunk classifications"
        const val TYPE_RECORDING = "recording"
        const val TYPE_CHUNK = "chunk"
    }

    private var recordingNotificationId: Int? = null
    private var chunkNotificationId: Int? = null

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRecordingNotification(title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()
        recordingNotificationId = notificationId

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", TYPE_RECORDING)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun showChunkNotification(title: String, message: String) {
        try {
            val notificationId = System.currentTimeMillis().toInt()
            chunkNotificationId = notificationId
            Log.d("NotificationHelper", "Creating chunk notification: id=$notificationId, title=$title, message=$message")

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("notification_type", TYPE_CHUNK)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .build()

            Log.d("NotificationHelper", "Showing chunk notification")
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error showing chunk notification", e)
        }
    }

    fun cancelRecordingNotification() {
        recordingNotificationId?.let { notificationManager.cancel(it) }
        recordingNotificationId = null
    }

    fun cancelChunkNotification() {
        chunkNotificationId?.let { notificationManager.cancel(it) }
        chunkNotificationId = null
    }
} 