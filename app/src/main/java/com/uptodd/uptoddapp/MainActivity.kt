package com.uptodd.uptoddapp

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.score.ScoreDatabaseDao
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var scoreDatabase: ScoreDatabaseDao

    val uiScope = CoroutineScope(Dispatchers.IO)

    lateinit var preferences: SharedPreferences

    var permissionGranted = false
    val permission1 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permission2 = Manifest.permission.READ_EXTERNAL_STORAGE

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreDatabase = UptoddDatabase.getInstance(this).scoreDatabaseDao
        uiScope.launch {
        }
        AllUtil.loadPreferences(this, "LOGIN_INFO")


        if (ContextCompat.checkSelfPermission(
                this,
                permission1
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission1, permission2), 100)
        } else permissionGranted = true

        val calendar: Calendar = Calendar.getInstance()

        Log.d("calendar", calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH).toString())

        preferences = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE)

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        Log.d("div", "MainActivity L128 $requestCode $permissions $grantResults")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 100) {
                permissionGranted = true
            } else
                ActivityCompat.requestPermissions(this, arrayOf(permission1, permission2), 100)
        } else
            ActivityCompat.requestPermissions(this, arrayOf(permission1, permission2), 100)
    }
}