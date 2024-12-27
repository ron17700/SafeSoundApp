package com.example.safesound.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object TimestampFormatter {
    fun formatTimestamp(timeMillis: Long): String {
        val date = Date(timeMillis)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return isoFormat.format(date)
    }
}