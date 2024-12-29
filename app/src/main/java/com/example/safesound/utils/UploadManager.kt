package com.example.safesound.utils

import android.util.Log
import com.example.safesound.data.records_list.RecordsApiService
import com.example.safesound.utils.TimestampFormatter.convertTimestampToIsoFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UploadManager @Inject constructor(
    @Named("recordsApi") private val recordsApi: RecordsApiService
) {

    private val uploadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun uploadChunk(
        recordId: String?,
        chunkFile: File?,
        startTime: Long,
        endTime: Long,
        onCompletion: (Boolean, String?) -> Unit
    ) {
        if (recordId == null || chunkFile == null || !chunkFile.exists() || chunkFile.length() == 0L) {
            Log.e("UploadManager", "Invalid chunk upload attempt.")
            onCompletion(false, "Invalid chunk file or record ID.")
            return
        }

        uploadScope.launch {
            try {
                val requestBody = RequestBody.create(MediaType.parse("audio/mpeg"), chunkFile)
                val chunkPart = MultipartBody.Part.createFormData("file", chunkFile.name, requestBody)
                val startTimeRequestBody = RequestBody.create(MediaType.parse("text/plain"), convertTimestampToIsoFormat(startTime))
                val endTimeRequestBody = RequestBody.create(MediaType.parse("text/plain"), convertTimestampToIsoFormat(endTime))

                val response = recordsApi.uploadRecordChunk(
                    recordId = recordId,
                    chunkFile = chunkPart,
                    startTime = startTimeRequestBody,
                    endTime = endTimeRequestBody
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onCompletion(true, null)
                    } else {
                        val error = response.errorBody()?.string() ?: "Unknown error"
                        onCompletion(false, error)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UploadManager", "Chunk upload failed: ${e.message}", e)
                    onCompletion(false, e.message)
                }
            }
        }
    }
}
