package com.example.safesound.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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

    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            val exp = json.optLong("exp", 0) - 60 // Buffer of 60 seconds
            System.currentTimeMillis() / 1000 >= exp
        } catch (e: Exception) {
            Log.e("TokenManager", "Error parsing token expiration", e)
            true
        }
    }

    fun isUserLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        return accessToken != null && !isTokenExpired(accessToken)
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
}