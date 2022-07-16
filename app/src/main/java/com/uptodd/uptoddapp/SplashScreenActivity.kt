package com.uptodd.uptoddapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.database.score.*
import com.uptodd.uptoddapp.databinding.ActivitySplashScreenBinding
import com.uptodd.uptoddapp.doctor.dashboard.DoctorDashboard
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.todoScreens.TodosListActivity
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.KidsPeriod
import com.uptodd.uptoddapp.utilities.createUptoddNotificationChannels
import com.uptodd.uptoddapp.utils.FilesUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.TimeUnit


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    companion object {
        const val KEY_NEW = "key_new_user"
    }

    private val preferences: SharedPreferences by lazy {
        getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    }
    private val scoreDatabase: ScoreDatabaseDao by lazy {
        UptoddDatabase.getInstance(this).scoreDatabaseDao
    }

    val uiScope = CoroutineScope(Dispatchers.IO)


    var permissionGranted = false
    val permission1 = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permission2 = Manifest.permission.READ_EXTERNAL_STORAGE

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupHeader()
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash_screen
        )

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        uiScope.launch {
            createScoreDatabase()
        }

        getSharedPreferences("navigation", Context.MODE_PRIVATE).edit()
            .remove("isDoctorDashboardNavigated").apply()

        val image = binding.uptoddSplashImage
        val text = binding.uptoddSplashText


        val dropdown: Animation = AnimationUtils.loadAnimation(this, R.anim.dropdown)
        val shake: Animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        text.visibility = View.VISIBLE
        image.startAnimation(dropdown)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    text.startAnimation(shake)
                }
            }
        }, 1000)

        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {


                }
            }
        }, 500)

        YoYo.with(Techniques.Bounce)
            .duration(500)
            .delay(900)
            .onEnd {
                openMainApp()
            }
            .playOn(image)

        createNotificationChannels()
    }

    private fun openMainApp() {
        //Only For Testing Purpose
        /*startActivity(Intent(this, LoginActivity::class.java))
        this.finishAffinity()
        finish()*/
        if (preferences.contains("userType") && preferences.getString(
                "userType",
                "Normal"
            ) == "Normal"
        ) {

            val notIntent = Intent(this, TodosListActivity::class.java)
            notIntent.putExtras(intent)

            if (intent.getIntExtra("showUp", 0) == 1) {
                notIntent.putExtra("showUp", 1)
                Log.d("ms splash", "Show up")
            }
            val notifyId = intent.getStringExtra("notifyId")
            notifyId.let {
                Log.d("notfiyId", it.toString())
                notIntent.putExtra("notifyId", it)
                notIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }


            val isNewUser = preferences.getBoolean(UserInfo::isNewUser.name, false)

            if (!isNewUser) {
                val addr = UptoddSharedPreferences.getInstance(this).getAddress(this)
                if (AllUtil.isUserPremium(this) && !AllUtil.isRow(this) && (addr == null || addr == "" || addr == "null")) {
                    val newLInt = Intent(this, LoginActivity::class.java)
                    newLInt.putExtra(KEY_NEW, 3)
                    startActivity(newLInt)
                } else {
                    startActivity(notIntent)
                }

            } else {
                val addr = UptoddSharedPreferences.getInstance(this).getAddress(this)
                val stage = UptoddSharedPreferences.getInstance(this).getStage()
                val dob = KidsPeriod(this).getKidsDob()
                val newLInt = Intent(this, LoginActivity::class.java)
                if (!AllUtil.isUserPremium(this)) {

                    newLInt.putExtra(KEY_NEW, 0)
                } else if (stage != "prenatal" && (dob == "null" || dob == null || dob == "")) {
                    newLInt.putExtra(KEY_NEW, 1)
                } else if (stage == "prenatal" && (addr == "" || addr == null || addr == "null")) {
                    Log.d("premium", "empty")
                    newLInt.putExtra(KEY_NEW, 3)
                } else if (stage == "postnatal") {
                    newLInt.putExtra(KEY_NEW, 2)
                }

                startActivity(newLInt)

            }
            this.finishAffinity()

        } else if (preferences.contains("userType") && preferences.getString(
                "userType",
                "Normal"
            ) == "Nanny"
        ) {

            startActivity(Intent(this, TodosListActivity::class.java))
            this.finishAffinity()

        } else if (preferences.contains("userType") && preferences.getString(
                "userType",
                "Normal"
            ) == "Google"
        ) {
            startActivity(Intent(this, UptoddWebsiteActivity::class.java))
            this.finishAffinity()
        } else if (preferences.contains("userType") && preferences.getString(
                "userType",
                "Normal"
            ) == "Facebook"
        ) {
            startActivity(Intent(this, UptoddWebsiteActivity::class.java))
            this.finishAffinity()
        } else if (preferences.contains("userType") && preferences.getString(
                "userType",
                "Normal"
            ) == "Doctor"
        ) {
            startActivity(Intent(this, DoctorDashboard::class.java))
            this.finishAffinity()
        } else if (preferences.contains(FilesUtils.DATASTORE.LoginType) && preferences.getString(
                FilesUtils.DATASTORE.LoginType,
                FilesUtils.DATASTORE.defValue
            ) == FilesUtils.DATASTORE.FREE_LOGIN
        ) {
            val intent = Intent(this, FreeParentingDemoActivity::class.java)
            intent.putExtra("showFreeParenting", true)
            startActivity(intent)
            this.finishAffinity()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            this.finishAffinity()
        }

    }

    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createUptoddNotificationChannels(
            "notification.music_player",
            "Media Player",
            "UpTodd Media Player",
        )

        notificationManager.createUptoddNotificationChannels(
            "activity.daily",
            "Daily Activities",
            "Reminder for daily activities",
        )

        notificationManager.createUptoddNotificationChannels(
            "activity.weekly",
            "Weekly Activities",
            "Reminder for weekly activities",
        )

        notificationManager.createUptoddNotificationChannels(
            "activity.monthly",
            "Monthly Activities",
            "Reminder for monthly activities",
        )

        notificationManager.createUptoddNotificationChannels(
            "activity.essential",
            "Essential Activities",
            "Reminder for essential activities",
        )

        notificationManager.createUptoddNotificationChannels(
            "fcm.push_notification",
            "Push Notification",
            "Push Notification from UpTodd",
        )
        notificationManager.createUptoddNotificationChannels(
            "wishes",
            "Wishes",
            "Wishes",
        )
        notificationManager.createUptoddNotificationChannels(
            "service",
            "Service",
            "Service",
        )
        notificationManager.createUptoddNotificationChannels(
            "subscription",
            "Subscription Reminder",
            "Reminder when subscription is ending.",
        )
        notificationManager.createUptoddNotificationChannels(
            "activitiesReminder",
            "Activities Reminder",
            "Alarm for Todos.",
        )
    }

    private suspend fun createScoreDatabase() {
        if (!UptoddSharedPreferences.getInstance(this).getScoreDatabaseCreatedStatus()) {

            uiScope.launch {
                withContext(Dispatchers.IO) {
                    val dailyScore = Score(DAILY_TODO, 0, 0)
                    scoreDatabase.insert(dailyScore)
                    val weeklyScore = Score(WEEKLY_TODO, 0, 0)
                    scoreDatabase.insert(weeklyScore)
                    val monthlyScore = Score(MONTHLY_TODO, 0, 0)
                    scoreDatabase.insert(monthlyScore)
                    val essentialsScore = Score(ESSENTIALS_TODO, 0, 0)
                    scoreDatabase.insert(essentialsScore)
                }
            }

            UptoddSharedPreferences.getInstance(this).setScoreDatabaseCreatedStatusTrue()
        } else return
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun setupHeader() {
        val b = OkHttpClient.Builder()
        b.addNetworkInterceptor(HttpLoggingInterceptor())
        b.readTimeout(120, TimeUnit.SECONDS)
        b.writeTimeout(120, TimeUnit.SECONDS)
        b.connectTimeout(120, TimeUnit.SECONDS)

        b.addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()

            //add auth token in header
            var token = AllUtil.getAuthToken()
            val request = original.newBuilder()
                .header("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        AndroidNetworking.initialize(applicationContext, b.build())
    }

}