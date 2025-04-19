package com.example.safesound.services

import android.util.Log
import com.example.safesound.data.auth.TokenManager
import com.example.safesound.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SafeSoundMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @Inject
    lateinit var tokenManager: TokenManager

    companion object {
        private const val TAG = "SafeSoundMessagingService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")

        remoteMessage.data.let { data ->
            val type = data["type"] ?: NotificationHelper.TYPE_RECORDING
            val title = data["title"] ?: "SafeSound"
            val message = data["message"] ?: ""

            Log.d(TAG, "Processing notification: type=$type, title=$title, message=$message")

            when (type) {
                NotificationHelper.TYPE_RECORDING -> handleRecordingNotification(title, message)
                NotificationHelper.TYPE_CHUNK -> handleChunkNotification(title, message)
            }
        }

        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "SafeSound"
            val body = notification.body ?: ""
            Log.d(TAG, "Showing notification: title=$title, body=$body")
            notificationHelper.showRecordingNotification(title, body)
        }
    }

    private fun handleRecordingNotification(title: String, message: String) {
        notificationHelper.showRecordingNotification(title, message)
    }

    private fun handleChunkNotification(title: String, message: String) {
        notificationHelper.showChunkNotification(title, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken called with token: $token")
        tokenManager.saveFcmToken(token)
        Log.d(TAG, "FCM Token for testing: $token")
    }
} 