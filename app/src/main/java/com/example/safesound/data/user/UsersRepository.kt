package com.example.safesound.data.user

import android.util.Log
import com.example.safesound.data.auth.TokenManager
import kotlinx.coroutines.withContext
import com.example.safesound.utils.ErrorParser
import com.example.safesound.utils.Result
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class User(val _id: String, val userName: String, val email: String, var role: String, val profileImage: String?)

@Singleton
class UsersRepository @Inject constructor(
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
                Log.e("RecordsRepository", errorMessage, e)
                Result(success = false, errorMessage = errorMessage)
            }
        }
    }
}