package com.example.hackerstudent

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClientApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}