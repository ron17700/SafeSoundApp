package com.example.safesound.utils

import org.json.JSONObject
import retrofit2.HttpException

object ErrorParser {
    fun parseHttpError(e: Throwable): String {
        return when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val json = JSONObject(errorBody)
                        json.getString("error")
                    } catch (jsonException: Exception) {
                        "An error occurred: ${e.message()}"
                    }
                } else {
                    "An unexpected error occurred. Please try again."
                }
            }
            else -> e.message ?: "An unexpected error occurred. Please try again."
        }
    }
}