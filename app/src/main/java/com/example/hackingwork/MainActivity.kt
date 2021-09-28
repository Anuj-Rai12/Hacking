package com.example.hackingwork

import android.app.NotificationManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.hackingwork.databinding.ActivityMainBinding
import com.example.hackingwork.utils.GetConstStringObj
import com.example.hackingwork.utils.GetCourseContent
import com.example.hackingwork.utils.Helper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

const val TAG = "ANUJ"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var emailAuthLink: String? = null
        var wifiManager: WifiManager? = null
        var getCourseContent: GetCourseContent? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "onCreate: Created Auth Activity")
        intent.extras?.keySet()?.forEach { s ->
            if (s == GetConstStringObj.VERSION)
                (intent.extras?.getString(s))?.let {
                    val getCourse = Helper.deserializeFromJson<GetCourseContent>(it)
                    getCourseContent = getCourse
                    val notificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(23)
                    Log.i(TAG, "onCreate: $getCourseContent")
                }
        }
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (intent != null && intent.data != null) {
            if (FirebaseAuth.getInstance().isSignInWithEmailLink(intent.data!!.toString())) {
                emailAuthLink = intent.data!!.toString()
            }
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.authFragmentView) as NavHostFragment
        navController = navHostFragment.findNavController()
        setupActionBarWithNavController(navController)
        Log.i(TAG, "onCreate: Testing Boss Logo")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}