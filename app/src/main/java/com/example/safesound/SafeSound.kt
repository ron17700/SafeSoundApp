package com.example.safesound

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import androidx.room.Room
import com.example.safesound.data.records.RecordDatabase
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.safesound.data.records.DeleteExpiredRecordsWorker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class SafeSound : Application() {
    lateinit var database: RecordDatabase

    override fun onCreate() {
        super.onCreate()
        val ttl = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        scheduleDeleteExpiredRecordsWorker(this, ttl)
    }

    private fun scheduleDeleteExpiredRecordsWorker(context: Context, ttl: Long) {
        val workManager = WorkManager.getInstance(context)
        val data = workDataOf("TTL" to ttl)
        val deleteExpiredRecordsRequest = PeriodicWorkRequestBuilder<DeleteExpiredRecordsWorker>(1, TimeUnit.DAYS)
            .setInputData(data)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DeleteExpiredRecords",
            ExistingPeriodicWorkPolicy.REPLACE,
            deleteExpiredRecordsRequest
        )
    }
}