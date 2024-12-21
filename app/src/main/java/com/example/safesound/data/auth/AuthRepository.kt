package com.example.safesound.data.auth

import android.net.Uri
import android.util.Log
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val profileImage: Uri?)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(val accessToken: String?, val refreshToken: String?, val message: String?)

@Singleton
class AuthRepository @Inject constructor(
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

    suspend fun register(email: String, password: String, profileImage: Uri?): Result<AuthResponse> {
        Log.d("AuthRepository", "Attempting to register with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = authApi.register(RegisterRequest(email, password, profileImage))
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
                authApi.logout()
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
