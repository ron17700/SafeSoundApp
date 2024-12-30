package com.example.safesound.data.auth

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<AuthResponse>

    @Multipart
    @POST("auth/register")
    suspend fun register(
        @Part("userName") userName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<AuthResponse>

    @POST("auth/refresh-token")
    fun refreshTokenSync(@Body request: RefreshTokenRequest): Call<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<AuthResponse>
}