package com.example.safesound.data.my_records

import android.util.Log
import kotlinx.coroutines.withContext
import android.net.Uri
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.Result
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


data class CreateRecordRequest(val name: String, val image: String?)
data class CreateRecordResponse(val _id: String)
data class UploadChunkResponse(val chunkId: String)
data class Record(val _id: String, val name: String, val createdAt: String, var recordClass: String, val image: String?)

@Singleton
class RecordsRepository @Inject constructor(
    @Named("recordsApi") private val recordsApi: RecordsApiService
) {
    suspend fun createRecord(name: String, imageFile: File?): Result<CreateRecordResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val imagePart = imageFile?.let {
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
                    MultipartBody.Part.createFormData("file", it.name, requestBody)
                }
                val nameRequestBody = RequestBody.create(MediaType.parse("text/plain"), name)
                val response = recordsApi.createRecord(nameRequestBody, imagePart)
                Log.d("RecordsRepository", "Record created: ${response.body()?._id}")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                println(e)
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun uploadRecordChunk(recordId: String, audioFile: MultipartBody.Part, startTime: String, endTime: String): Result<UploadChunkResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val idRequestBody = RequestBody.create(MediaType.parse("text/plain"), recordId)
                val startTimeRequestBody = RequestBody.create(MediaType.parse("text/plain"), startTime)
                val endTimeRequestBody = RequestBody.create(MediaType.parse("text/plain"), endTime)
                val response = recordsApi.uploadRecordChunk(idRequestBody, audioFile, startTimeRequestBody, endTimeRequestBody)
                Log.d("RecordsRepository", "Chunk uploaded: ${response.body()?.chunkId}")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun getAllRecords(): Result<List<Record>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsApi.getAllRecords()
                Log.d("RecordsRepository", "Fetched records: ${response.body()?.size}")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }
}