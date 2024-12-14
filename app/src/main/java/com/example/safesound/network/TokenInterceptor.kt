package com.example.safesound.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.safesound.data.TokenManager
import com.example.safesound.ui.auth.AuthenticationActivity
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class TokenInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    private val excludedPaths = listOf("/login", "/register", "/refresh-token")
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = chain.request().url().toString()

        if (excludedPaths.any { url.contains(it) }) {
            return chain.proceed(request)
        }

        var accessToken = tokenManager.getAccessToken()
        if (accessToken == null || tokenManager.isTokenExpired(accessToken)) {
            val refreshSuccessful = tokenManager.refreshToken()
            if (refreshSuccessful) {
                accessToken = tokenManager.getAccessToken()
            } else {
                tokenManager.clearTokens()
                redirectToLogin()
                throw IOException("Failed to refresh token")
            }
        }

        if (accessToken != null) {
            request = addAuthorizationHeader(request, accessToken)
        }
        return chain.proceed(request)
    }

    private fun addAuthorizationHeader(request: okhttp3.Request, token: String): okhttp3.Request {
        return request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    }

    private fun redirectToLogin() {
        mainHandler.post {
            val intent = Intent(context, AuthenticationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }
}
