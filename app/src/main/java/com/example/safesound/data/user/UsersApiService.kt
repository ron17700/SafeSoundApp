package com.example.safesound.data.user

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApiService {

    @GET("/user/{id}")
    suspend fun getCurrentUser(@Path("id") userId: String): Response<User>
}