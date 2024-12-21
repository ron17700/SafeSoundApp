package com.example.safesound.utils

data class Result<T>(val success: Boolean, val data: T? = null, val errorMessage: String? = null)