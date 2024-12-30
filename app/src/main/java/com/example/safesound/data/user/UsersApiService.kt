package com.example.safesound.data.user

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UsersApiService {

    @Multipart
    @PUT("/user/{id}")
    suspend fun updateCurrentUser(
        @Path("id") userId: String,
        @Part("userName") userName: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<User>
}