package com.example.hackerstudent

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

@HiltAndroidApp
class ClientApplication : Application() {
    companion object {
        private lateinit var myContext: WeakReference<Context>
        fun appContext() = myContext
    }

    override fun onCreate() {
        myContext = WeakReference(applicationContext)
        super.onCreate()
    }
}