package com.example.hackingwork

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AdminApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}