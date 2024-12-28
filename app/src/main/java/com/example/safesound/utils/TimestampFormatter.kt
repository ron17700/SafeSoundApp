package com.example.safesound.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object TimestampFormatter {
    fun convertTimestampToIsoFormat(timeMillis: Long): String {
        val date = Date(timeMillis)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return isoFormat.format(date)
    }

    fun formatIsoToTime(isoString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(isoString)
            outputFormat.format(date ?: isoString)
        } catch (e: Exception) {
            isoString
        }
    }
}