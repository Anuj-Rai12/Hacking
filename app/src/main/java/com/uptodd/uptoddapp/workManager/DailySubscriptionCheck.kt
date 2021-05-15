package com.uptodd.uptoddapp.workManager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DailySubscriptionCheck(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    val preferences: SharedPreferences =
        context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = preferences.edit()
    var userId = ""
    var token = ""
    val context1 = context

    override suspend fun doWork(): Result = coroutineScope {

        launch {
            if (preferences.contains("uid") && preferences.getString("uid", "") != null) {
                userId = preferences.getString("uid", "")!!
                token = preferences.getString("token", "")!!

                AndroidNetworking.get("https://uptodd.com/api/appusers/checksubscription/{userId}")
                    .addHeaders("Authorization", "Bearer $token")
                    .addPathParameter("userId", userId)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response.get("status") == "Success") {
                                val subscriptionActive =
                                    (response.get("data") as JSONObject).getInt("subscriptionActive")        //yyyy-MM-dd
                                //displayNotification(subscriptionActive.toString(), subscriptionActive.toString())
                                val lastUserPasswordUpdate: String? =
                                    (response.get("data") as JSONObject).getString("userPassUpdateTime")

                                val lastNannyPasswordUpdate: String? =
                                    (response.get("data") as JSONObject).getString("nannyPassUpdateTime")

                                if (subscriptionActive != 1)
                                    logout()

                                var currentDate: Date?

                                val launchTime = preferences.getLong("LaunchTime", 0L)

                                // compare the app launch time with recieved password update time

                                if (launchTime != 0L) {
                                    val calendar = Calendar.getInstance()
                                    calendar.timeInMillis = launchTime
                                    currentDate = calendar.time
                                } else {
                                    currentDate = null
                                }

                                if (lastUserPasswordUpdate != "null" && currentDate != null) {
                                    val userDateParsed =
                                        SimpleDateFormat("uuuu-MM-dd").parse(lastUserPasswordUpdate)
                                    if (currentDate.before(userDateParsed)) {
                                        // logout user if password is updated.
                                        logout()
                                    }

                                }

                                if (lastNannyPasswordUpdate != "null" && currentDate != null) {
                                    val nannyDateParsed =
                                        SimpleDateFormat("uuuu-MM-dd").parse(lastNannyPasswordUpdate)

                                    if (currentDate.before(nannyDateParsed)) {
                                        // logout nanny if password is updated.
                                        logout()
                                    }

                                }
                            }
                        }

                        override fun onError(error: ANError?) {
                            Log.d("div", "LoginViewModel L67 $error")
                            if (error!!.errorCode != 0) {
                                Log.d("div", "onError errorCode : " + error.errorCode)
                                Log.d("div", "onError errorBody : " + error.errorBody)
                                Log.d("div", "onError errorDetail : " + error.errorDetail)
                            } else {
                                // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                Log.d("div", "onError errorDetail : " + error.errorDetail)
                            }
                        }

                    })
            }
        }

        Result.success()
    }

    private fun logout() {
        editor.putBoolean("loggedIn", false)
        editor.remove("language")
        editor.commit()
        var user = ""
        Log.i("debug", "Logging user out.")

        if (preferences.contains("userType") && preferences.getString("userType", "")!!
                .isNotEmpty()
        ) {
            user = preferences.getString("userType", "")!!
        } else {
            return
        }

        if (user == "Nanny") {
            val uid = preferences.getString("uid", "")
            AndroidNetworking.get("https://uptodd.com/api/nannylogout/{userId}")
                .addHeaders("Authorization", "Bearer $token")
                .addPathParameter("userId", uid)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("debug", "$response")
                        editor.remove("LaunchTime")
                        context1.startActivity(Intent(context1, LoginActivity::class.java))
                    }

                    override fun onError(anError: ANError?) {
                        Log.i("debug", "${anError?.message}")
                    }

                })
        } else if (user == "Normal") {
            val uid = preferences.getString("userId", "")
            AndroidNetworking.get("https://uptodd.com/api/userlogout/{userId}")
                .addHeaders("Authorization", "Bearer $token")
                .addPathParameter("userId", uid)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("debug", "$response")
                        editor.remove("LaunchTime")
                        context1.startActivity(Intent(context1, LoginActivity::class.java))
                    }

                    override fun onError(anError: ANError?) {
                        Log.i("debug", "${anError?.message}")
                        context1.startActivity(Intent(context1, LoginActivity::class.java))
                        editor.remove("LaunchTime")
                    }
                })

            cancelAllWorkManagers(context1.applicationContext as Application, context1)

            AllUtil.unregisterToken()
        }
    }

    private fun displayNotification(task: String, description: String) {
        val manager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("div", "Divyanshu", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(applicationContext, "div")
            .setContentTitle(task)
            .setContentText(description)
            .setSmallIcon(R.mipmap.ic_launcher)

        manager.notify(1, builder.build())
    }
}


