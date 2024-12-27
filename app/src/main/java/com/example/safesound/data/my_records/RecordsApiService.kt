package com.example.safesound.data.my_records

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RecordsApiService {

    @Multipart
    @POST("/record")
    suspend fun createRecord(
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<CreateRecordResponse>

    @GET("/record")
    suspend fun getAllRecords(): Response<List<Record>>

    @GET("/record/{id}")
    suspend fun getRecordById(@Path("id") recordId: String): Response<Record>

    @Multipart
    @POST("/chunk/{recordId}")
    suspend fun uploadRecordChunk(
        @Path("recordId") recordId: RequestBody,
        @Part chunkFile: MultipartBody.Part,
        @Part("startTime") startTime: RequestBody,
        @Part("endTime") endTime: RequestBody
    ): Response<UploadChunkResponse>
}