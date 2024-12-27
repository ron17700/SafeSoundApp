package com.example.safesound.data.auth

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body credentials: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/refresh-token")
    fun refreshTokenSync(@Body request: RefreshTokenRequest): Call<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<AuthResponse>
}