package com.example.safesound.data.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.RequestHelper
import com.example.safesound.utils.RequestHelper.toRequestBody
import com.example.safesound.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class LoginRequest(val email: String, val password: String)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(val accessToken: String?, val refreshToken: String?, val message: String?)

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("unsecureAuthApi") private val unsecureAuthApi: AuthApiService,
    @Named("authApi") private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    fun isUserLoggedIn(): Boolean {
        return tokenManager.isUserLoggedIn()
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        Log.d(TAG, "Attempting to log in with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = unsecureAuthApi.login(LoginRequest(email, password))
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                response.body()?.accessToken?.let {
                    response.body()?.refreshToken?.let { refreshToken ->
                        tokenManager.saveTokens(it, refreshToken)
                        updateFcmToken().let { fcmResult ->
                            if (!fcmResult.success) {
                                Log.e(TAG, "Failed to update FCM token: ${fcmResult.errorMessage}")
                            }
                        }
                    }
                }
                Log.d(TAG, "Login successful and tokens saved")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e(TAG, "Login failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun register(userName: String, email: String, password: String, profileImage: Uri?): Result<AuthResponse> {
        Log.d(TAG, "Attempting to register with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                var imagePart: MultipartBody.Part? = null;
                if (profileImage != null) {
                    imagePart = RequestHelper.imageUriToMultiPart(context, profileImage, "profile_image")
                }
                val response = authApi.register(
                    userName.toRequestBody(), email.toRequestBody(), password.toRequestBody(), imagePart
                )
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                Log.d(TAG, "Registration successful")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e(TAG, "Registration failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun logout(): Result<AuthResponse> {
        Log.d(TAG, "Attempting to log out")
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.logout()
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                tokenManager.clearAllTokens()
                Log.d(TAG, "Logout successful and tokens cleared")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e(TAG, "Logout failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun updateFcmToken(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getFcmToken() ?: return@withContext Result(
                success = false,
                errorMessage = "Failed to get FCM token"
            )

            val requestBody = RequestHelper.createJsonRequestBody(
                mapOf(
                    "fcmToken" to token,
                    "deviceId" to android.os.Build.MODEL
                )
            )

            val response = authApi.updateFcmToken(requestBody)
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorBody()?.string())
            }

            Log.d(TAG, "FCM token updated successfully for device: ${android.os.Build.MODEL}")
            Result(success = true)
        } catch (e: Exception) {
            val errorMessage = ErrorParser.parseHttpError(e)
            Log.e(TAG, "Error updating FCM token: $errorMessage", e)
            Result(success = false, errorMessage = errorMessage)
        }
    }
}
