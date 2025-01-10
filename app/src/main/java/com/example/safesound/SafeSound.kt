package com.example.safesound

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import androidx.room.Room
import com.example.safesound.data.records.RecordDatabase

@HiltAndroidApp
class SafeSound : Application() {
    lateinit var database: RecordDatabase

    override fun onCreate() {
        super.onCreate()
    }
}