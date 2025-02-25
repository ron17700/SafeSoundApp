package com.example.safesound.data.records

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Okio
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface RecordsApiService {

    @Multipart
    @POST("/record")
    suspend fun createRecord(
        @Part("name") name: RequestBody,
        @Part("latitude") latitude: Double?,
        @Part("longitude") longitude: Double?,
        @Part("isPublic") isPublic: Boolean,
        @Part image: MultipartBody.Part?
    ): Response<Record>

    @Multipart
    @PUT("/record/{id}")
    suspend fun updateRecord(
        @Path("id") recordId: String,
        @Part("name") name: RequestBody,
        @Part("isPublic") isPublic: Boolean,
        @Part image: MultipartBody.Part?
    ): Response<Record>

    @DELETE("/record/{id}")
    suspend fun deleteRecord(@Path("id") recordId: String): Response<Okio>

    @POST("/record/{id}/like")
    suspend fun likeRecord(@Path("id") recordId: String): Response<Okio>

    @GET("/record")
    suspend fun getAllRecords(): Response<List<Record>>

    @GET("/record/public")
    suspend fun getAllPublicRecords(): Response<List<Record>>

    @GET("/record/{recordId}/chunk")
    suspend fun getAllChunks(@Path("recordId") recordId: String): Response<List<Chunk>>

    @Multipart
    @POST("/record/{recordId}/chunk")
    suspend fun uploadRecordChunk(
        @Path("recordId") recordId: String,
        @Part chunkFile: MultipartBody.Part,
        @Part("startTime") startTime: RequestBody,
        @Part("endTime") endTime: RequestBody
    ): Response<Okio>
}