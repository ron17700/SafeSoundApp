package com.example.safesound.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body credentials: RegisterRequest)

    @POST("auth/logout")
    suspend fun logout()

    @POST("auth/refresh-token")
    suspend fun refreshToken(): AuthResponse
}