package com.example.safesound.data.records

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteExpiredRecordsWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val recordDao: RecordDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val ttl = inputData.getLong("TTL", 0L)
                val currentTime = System.currentTimeMillis()
                val expiryTime = currentTime - ttl
                recordDao.deleteExpiredRecords(expiryTime)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}