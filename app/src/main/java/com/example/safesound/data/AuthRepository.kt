// AuthRepository.kt
package com.example.safesound.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.safesound.network.NetworkModule
import com.example.safesound.network.TokenInterceptor
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val profileImage: Uri?)
data class RefreshTokenRequest(val refreshToken: String)
data class AuthResponse(val accessToken: String?, val refreshToken: String?, val message: String?)

class AuthRepository(private val context: Context) {

    private val authRetrofit: Retrofit = NetworkModule.provideAuthRetrofit()
    private val authApiAuth: AuthApiService = authRetrofit.create(AuthApiService::class.java)

    private val tokenManager: TokenManager = TokenManager(context, authApiAuth)
    private val tokenInterceptor: TokenInterceptor = TokenInterceptor(context, tokenManager)

    private val appRetrofit: Retrofit = NetworkModule.provideAppRetrofit(context, tokenInterceptor)
    private val authApiApp: AuthApiService = appRetrofit.create(AuthApiService::class.java)


    fun isUserLoggedIn(): Boolean {
        return tokenManager.isUserLoggedIn()
    }

    suspend fun login(email: String, password: String): Result {
        Log.d("AuthRepository", "Attempting to log in with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiAuth.login(LoginRequest(email, password))
                response.accessToken?.let {
                    response.refreshToken?.let { refreshToken ->
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

    suspend fun register(email: String, password: String, profileImage: Uri?): Result {
        Log.d("AuthRepository", "Attempting to register with email: $email")
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiAuth.register(RegisterRequest(email, password, profileImage))
                Log.d("AuthRepository", "Registration successful")
                Result(success = true)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("AuthRepository", "Registration failed: $errorMessage", e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun logout(): Result {
        Log.d("AuthRepository", "Attempting to log out")
        return withContext(Dispatchers.IO) {
            try {
                authApiApp.logout()
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
