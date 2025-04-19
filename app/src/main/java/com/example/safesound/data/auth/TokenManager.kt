package com.example.safesound.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.safesound.data.user.User
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("unsecureAuthApi") private val unsecureAuthApi: AuthApiService,
) {
    companion object {
        private const val PREFS_FILENAME = "SafeSoundPrefs"
        private const val ACCESS_TOKEN_KEY = "accessToken"
        private const val REFRESH_TOKEN_KEY = "refreshToken"
        private const val FCM_TOKEN_KEY = "fcmToken"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFS_FILENAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply()
        }
        Log.d("TokenManager", "Tokens saved successfully")
    }

    fun clearTokens() {
        sharedPreferences.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
        Log.d("TokenManager", "Tokens cleared successfully")
    }

    fun isTokenExpired(): Boolean {
        val json = getTokenJson()
        val exp = json.optLong("exp", 0) - 60 // Buffer of 60 seconds
        return System.currentTimeMillis() / 1000 >= exp
    }

    fun isUserLoggedIn(): Boolean {
        return !isTokenExpired()
    }

    fun getUser(): User {
        val json = getTokenJson()
        return User(_id = json.optString("userId", ""),
            userName = json.optString("userName", ""),
            email = json.optString("email", ""),
            role = json.optString("role", ""),
            profileImage = json.optString("profileImage", ""))

    }

    /**
     * Refreshes the access token synchronously.
     * Returns true if the refresh was successful, false otherwise.
     */
    fun refreshToken(): Boolean {
        val refreshToken = getRefreshToken() ?: return false
        return try {
            val call: Call<AuthResponse> = unsecureAuthApi.refreshTokenSync(RefreshTokenRequest(refreshToken))
            val response: Response<AuthResponse> = call.execute()
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.accessToken != null) {
                    saveTokens(authResponse.accessToken, refreshToken)
                    Log.d("TokenManager", "Token refresh successful")
                    true
                } else {
                    Log.e("TokenManager", "Invalid token response during refresh")
                    false
                }
            } else {
                Log.e("TokenManager", "Token refresh failed with code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Exception during token refresh", e)
            false
        }
    }

    private fun getTokenJson(): JSONObject {
        val accessToken = getAccessToken() ?: ""
        val parts = accessToken.split(".")
        if (parts.size != 3) return JSONObject()
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        return JSONObject(payload)
    }

    fun saveFcmToken(token: String) {
        sharedPreferences.edit().putString(FCM_TOKEN_KEY, token).apply()
        Log.d("TokenManager", "FCM token saved successfully")
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString(FCM_TOKEN_KEY, null)
    }

    fun clearAllTokens() {
        sharedPreferences.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
        Log.d("TokenManager", "All tokens cleared successfully")
    }
}