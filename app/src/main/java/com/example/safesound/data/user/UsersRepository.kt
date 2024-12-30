package com.example.safesound.data.user

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.safesound.data.auth.TokenManager
import kotlinx.coroutines.withContext
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.RequestHelper
import com.example.safesound.utils.RequestHelper.toRequestBody
import com.example.safesound.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class User(val _id: String, val userName: String, val email: String, var role: String, val profileImage: String?)

@Singleton
class UsersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("usersApi") private val usersApi: UsersApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = tokenManager.getUser()
                Log.d("UsersRepository", "Fetched user: ${user._id}")
                Result(success = true, data = user)
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("UsersRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }

    suspend fun updateCurrentUser(userName: String, profileImage: Uri?): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = tokenManager.getUser()
                var imagePart: MultipartBody.Part? = null;
                if (profileImage != null) {
                    imagePart = RequestHelper.imageUriToMultiPart(context, profileImage, "profile_image")
                }
                val response = usersApi
                    .updateCurrentUser(user._id, userName.toRequestBody(), imagePart)
                if (!response.isSuccessful) {
                    throw IllegalStateException(response.errorBody()?.string())
                }
                tokenManager.refreshToken()
                Log.d("UsersRepository", "User updated: ${response.body()?._id}")
                Result(success = true, data = response.body())
            } catch (e: Exception) {
                val errorMessage = ErrorParser.parseHttpError(e)
                Log.e("UsersRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }
}