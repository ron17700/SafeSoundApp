package com.example.safesound.data.auth

import android.app.Activity
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
data class LoginWithGoogle(val email: String)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(val accessToken: String?, val refreshToken: String?, val message: String?)

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("unsecureAuthApi") private val unsecureAuthApi: AuthApiService,
    @Named("authApi") private val authApi: AuthApiService,
    private val tokenManager: TokenManager
) {
    fun isUserLoggedIn(): Boolean {
        return tokenManager.isUserLoggedIn()
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        Log.d("AuthRepository", "Attempting to log in with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = unsecureAuthApi.login(LoginRequest(email, password))
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                response.body()?.accessToken?.let {
                    response.body()?.refreshToken?.let { refreshToken ->
                        tokenManager.saveTokens(it, refreshToken)
                    }
                }
                Log.d("AuthRepository", "Login successful and tokens saved")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("AuthRepository", "Login failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun signInWithGoogle(email: String): Result<AuthResponse> {
        Log.d("AuthRepository", "Attempting to sign in with Google, with gmail: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = unsecureAuthApi.loginWithGoogle(LoginWithGoogle(email))
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                response.body()?.accessToken?.let {
                    response.body()?.refreshToken?.let { refreshToken ->
                        tokenManager.saveTokens(it, refreshToken)
                    }
                }
                Log.d("AuthRepository", "Google Login successful and tokens saved")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("AuthRepository", "Google Login failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun register(userName: String, email: String, password: String, profileImage: Uri?): Result<AuthResponse> {
        Log.d("AuthRepository", "Attempting to register with email: $email")
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
                Log.d("AuthRepository", "Registration successful")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("AuthRepository", "Registration failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun logout(): Result<AuthResponse> {
        Log.d("AuthRepository", "Attempting to log out")
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.logout()
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                tokenManager.clearTokens()
                Log.d("AuthRepository", "Logout successful and tokens cleared")
                    Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("AuthRepository", "Logout failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }
}
