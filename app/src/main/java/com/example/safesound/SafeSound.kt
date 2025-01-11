package com.example.safesound

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SafeSound : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}