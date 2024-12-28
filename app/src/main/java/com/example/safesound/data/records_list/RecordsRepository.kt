package com.example.safesound.data.records_list

import android.util.Log
import kotlinx.coroutines.withContext
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.Result
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Okio
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


data class CreateRecordRequest(val name: String, val isPublic: Boolean, val image: String?)
data class CreateRecordResponse(val _id: String)
data class UploadChunkResponse(val chunkId: String)
data class Record(val _id: String, val name: String, val createdAt: String, var recordClass: String, val image: String?)
data class Chunk(val _id: String, val name: String, val startTime: String, val endTime: String, var chunkClass: String, val summary: String, val audioFilePath: String)

@Singleton
class RecordsRepository @Inject constructor(
    @Named("recordsApi") private val recordsApi: RecordsApiService
) {
    suspend fun createRecord(name: String, isPublic: Boolean, imageFile: File?): Result<CreateRecordResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val imagePart = imageFile?.let {
                    val requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
                    MultipartBody.Part.createFormData("file", it.name, requestBody)
                }
                val nameRequestBody = RequestBody.create(MediaType.parse("text/plain"), name)
                val response = recordsApi.createRecord(nameRequestBody, isPublic, imagePart)
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

    suspend fun deleteRecord(recordId: String): Result<Okio> {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsApi.deleteRecord(recordId)
                Log.d("RecordsRepository", "Record deleted: $recordId")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun getAllChunks(recordId: String): Result<List<Chunk>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsApi.getAllChunks(recordId)
                Log.d("RecordsRepository", "Fetched chunks: ${response.body()?.size}")
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