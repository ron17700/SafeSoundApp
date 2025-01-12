package com.example.safesound.data.records

import androidx.room.PrimaryKey
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.safesound.data.user.User
import com.example.safesound.models.records.RecordDao
import com.example.safesound.models.records.RecordEntity
import com.example.safesound.models.records.toRecord
import kotlinx.coroutines.withContext
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.RequestHelper
import com.example.safesound.utils.RequestHelper.toRequestBody
import com.example.safesound.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okio.Okio
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class Record(
    @PrimaryKey val _id: String,
    val name: String,
    val createdAt: String,
    var recordClass: String,
    val public: Boolean,
    val isFavorite: Boolean,
    val userId: User?,
    val latitude: Double?,
    val longitude: Double?,
    val image: String?
)

data class Chunk(
    val _id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    var chunkClass: String,
    val summary: String,
    val audioFilePath: String
)

@Singleton
class RecordsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("recordsApi") private val recordsApi: RecordsApiService,
    private val recordDao: RecordDao
) {
    suspend fun createRecord(
        name: String,
        isPublic: Boolean,
        latitude: Double?,
        longitude: Double?,
        imageUri: Uri?
    ): Result<Record> {
        return withContext(Dispatchers.IO) {
            try {
                var imagePart: MultipartBody.Part? = null
                if (imageUri != null) {
                    imagePart = RequestHelper.imageUriToMultiPart(context, imageUri, "record_image")
                }
                val response = recordsApi.createRecord(
                    name.toRequestBody(),
                    latitude,
                    longitude,
                    isPublic,
                    imagePart
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
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

    suspend fun updateRecord(
        recordId: String,
        name: String,
        isPublic: Boolean,
        imageUri: Uri?
    ): Result<Record> {
        return withContext(Dispatchers.IO) {
            try {
                var imagePart: MultipartBody.Part? = null
                if (imageUri != null) {
                    imagePart = RequestHelper.imageUriToMultiPart(context, imageUri, "record_image")
                }
                val response =
                    recordsApi.updateRecord(recordId, name.toRequestBody(), isPublic, imagePart)
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                Log.d("RecordsRepository", "Record updated: ${response.body()?._id}")
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
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                Log.d("RecordsRepository", "Record deleted: $recordId")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun likeRecord(recordId: String): Result<Okio> {
        return withContext(Dispatchers.IO) {
            try {
                val response = recordsApi.likeRecord(recordId)
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                Log.d("RecordsRepository", "Record like: $recordId")
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
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                Log.d("RecordsRepository", "Fetched chunks: ${response.body()?.size}")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun getAllRecordsCached(
        isMyRecords: Boolean,
        refresh: Boolean = false
    ): Result<List<Record>> {
        return withContext(Dispatchers.IO) {
            val cachedRecords = recordDao.getRecordsByType(isMyRecords)
            if (refresh || cachedRecords.isEmpty()) {
                val apiResult = getAllRecords(isMyRecords)
                if (apiResult.success) {
                    Result(
                        success = true,
                        data = apiResult.data?.map { it.toRecord() },
                        errorMessage = null
                    )
                } else {
                    Result(success = false, errorMessage = apiResult.errorMessage)
                }
            } else {
                launch { getAllRecords(isMyRecords) }
                Result(
                    success = true,
                    data = cachedRecords.map { it.toRecord() },
                    errorMessage = null
                )
            }
        }
    }

    private suspend fun getAllRecords(isMyRecords: Boolean): Result<List<RecordEntity>> {
        val currentTime = System.currentTimeMillis()
        try {
            val response = if (isMyRecords) {
                recordsApi.getAllRecords()
            } else {
                recordsApi.getAllPublicRecords()
            }
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorBody()?.string())
            } else {
                val records = response.body()?.let { records ->
                    val recordEntities = records.map { record: Record ->
                        RecordEntity(
                            record._id,
                            record.name,
                            record.createdAt,
                            record.recordClass,
                            record.public,
                            record.isFavorite,
                            record.userId?._id ?: "",
                            record.userId?.email ?: "",
                            record.userId?.userName ?: "",
                            record.userId?.role ?: "",
                            record.userId?.profileImage ?: "",
                            record.latitude,
                            record.longitude,
                            record.image,
                            currentTime,
                            isMyRecords
                        )
                    }
                    recordDao.deleteRecordsByType(isMyRecords)
                    recordDao.insertAll(recordEntities)
                    recordEntities.forEach { recordEntity ->
                        recordDao.updateCacheTimestampByType(
                            recordEntity.id,
                            currentTime,
                            isMyRecords
                        )
                    }
                    recordEntities
                } ?: emptyList()
                Log.d("RecordsRepository", "Fetched records: ${response.body()?.size}")
                return Result(success = true, data = records)
            }
        } catch (e: Exception) {
            val errorMessage = ErrorParser.parseHttpError(e)
            Log.e("RecordsRepository", errorMessage, e)
            return Result(success = false, errorMessage = errorMessage)
        }
    }

}