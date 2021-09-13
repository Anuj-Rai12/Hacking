package com.uptodd.uptoddapp.utilities

import androidx.navigation.NavController


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uptodd.uptoddapp.BuildConfig
import com.uptodd.uptoddapp.LoginActivity
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryBoosterFiles
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.resource.ResourceFiles
import com.uptodd.uptoddapp.database.nonpremium.NonPremiumAccount
import com.uptodd.uptoddapp.database.referrals.ReferredListItemDoctor
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.database.support.Experts
import com.uptodd.uptoddapp.database.support.Sessions
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.databinding.DialogExtendSubscriptionBinding
import com.uptodd.uptoddapp.media.player.BackgroundPlayer
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.support.view.TicketMessage
import com.uptodd.uptoddapp.ui.upgrade.UpgradeItem
import com.uptodd.uptoddapp.workManager.cancelUptoddWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class AllUtil{
    companion object{

        private lateinit var sharedPreferences: SharedPreferences


         fun logout(context: Context,activity: Activity) {
            val dialogBinding = DataBindingUtil.inflate<DialogExtendSubscriptionBinding>(
                LayoutInflater.from(context), R.layout.dialog_extend_subscription, null, false
            )
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(dialogBinding.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBinding.textView.text = context.getString(R.string.are_you_sure_logout)
            dialogBinding.buttonYes.setOnClickListener {
                if(UpToddMediaPlayer.isPlaying)
                {
                    UpToddMediaPlayer.upToddMediaPlayer.stop()
                    val intent = Intent(context, BackgroundPlayer::class.java)
                    intent.putExtra("toRun", false)
                    intent.putExtra("musicType", "music")
                    context.sendBroadcast(intent)
                }
                val preferences =context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putBoolean("loggedIn", false)
                editor.remove("language")
                editor.commit()

                AndroidNetworking.get("https://www.uptodd.com/api/userlogout/{userId}")
                    .addHeaders("Authorization", "Bearer ${getAuthToken()}")
                    .addPathParameter("userId", getUserId().toString())
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            Log.i("debug", "$response")
                            editor.remove("LaunchTime")
                        }

                        override fun onError(anError: ANError?) {
                            Log.i("debug", "${anError?.message}")
                        }
                    })


                WorkManager.getInstance(context).cancelUptoddWorker()

                unregisterToken()

                UptoddSharedPreferences.getInstance(context).clearAllPreferences()

                CoroutineScope(Dispatchers.IO).launch {
                    UptoddDatabase.getInstance(context).clearAllTables()
                }

                context.startActivity(
                    Intent(
                        activity,
                        LoginActivity::class.java
                    )
                )
                activity?.finishAffinity()
            }
            dialogBinding.buttonNo.setOnClickListener { dialog.dismiss() }
            dialog.setCancelable(false)
            dialog.show()

        }

        fun checkForAppUpdate(navController: NavController)
        {
            val json= JSONObject().apply {
                put("version", "2.6".toDouble())
                put("deviceType","android")
            }

            AndroidNetworking.post("https://www.uptodd.com/api/isValidVersion")
                .addHeaders("Authorization", "Bearer ${getAuthToken()}")
                .addJSONObjectBody(json)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object :JSONObjectRequestListener
                {
                    override fun onResponse(response: JSONObject?) {

                       val res=response?.get("data") as Int

                        Log.d("data version","$res")

                        if(res==0)
                        {
                            try {
                                navController?.navigate(R.id.action_homePageFragment_to_fragmentUpdateApp2)
                            }
                            catch (e:Exception)
                            {
                                navController?.navigate(R.id.action_loginFragment_to_fragmentUpdateApp)
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d("data version error","${anError?.errorDetail}")

                    }

                })
        }

        fun logoutOnly(context: Context)
        {

                val preferences =context.getSharedPreferences("LOGIN_INFO", Context.MODE_PRIVATE)
                val editor = preferences.edit()
                editor.putBoolean("loggedIn", false)
                editor.remove("language")
                editor.commit()




                AndroidNetworking.get("https://www.uptodd.com/api/userlogout/{userId}")
                    .addHeaders("Authorization", "Bearer ${getAuthToken()}")
                    .addPathParameter("userId", getUserId().toString())
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            Log.i("debug", "$response")
                            editor.remove("LaunchTime")
                        }

                        override fun onError(anError: ANError?) {
                            Log.i("debug", "${anError?.message}")
                        }
                    })


                WorkManager.getInstance(context).cancelUptoddWorker()

                unregisterToken()

                UptoddSharedPreferences.getInstance(context).clearAllPreferences()

                CoroutineScope(Dispatchers.IO).launch {
                    UptoddDatabase.getInstance(context).clearAllTables()
                }
        }

        fun registerToken(userType:String) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.i("FCM", "Token: $token")

                val jsonObject = JSONObject()
                jsonObject.put("deviceToken", token)

                var url=""
                if(userType=="doctor")
                    url="https://www.uptodd.com/api/doctor/deviceToken/{userId}"
                else if(userType=="normal" || userType=="nanny")
                    url="https://www.uptodd.com/api/appusers/deviceToken/{userId}"

                if(userType=="normal" || userType=="nanny" || userType=="doctor") {
                    Log.d("div","AllUtil L55 $url $userType ${getUserId()}")
                    AndroidNetworking.put(url)
                        .addHeaders("Authorization","Bearer $token")
                        .addPathParameter("userId", getUserId().toString())
                        .addJSONObjectBody(jsonObject)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject?) {
                                Log.i("tokenResult", response.toString())
                            }

                            override fun onError(anError: ANError?) {
                                logApiError(anError!!)
                            }
                        })
                }
            })
        }

        fun unregisterToken() {
            val jsonObject = JSONObject()
            jsonObject.put("deviceToken", "")
            AndroidNetworking.put("https://www.uptodd.com/api/appusers/deviceToken/{userId}")
                .addPathParameter("userId", getUserId().toString())
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.i("tokenResult", response.toString())
                    }

                    override fun onError(anError: ANError?) {
                        logApiError(anError!!)
                    }
                })
        }



        fun loadPreferences(context: Context, name: String){
            sharedPreferences = context.getSharedPreferences(name, AppCompatActivity.MODE_PRIVATE)
        }




        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


        fun getAllTickets(jsonString: String): ArrayList<Ticket> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Ticket?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<Ticket>
        }

        fun getAllSessions(jsonString: String): ArrayList<Sessions> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Sessions?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<Sessions>
        }

        fun getAllExperts(jsonString: String): ArrayList<Experts> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Experts?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<Experts>
        }
        fun getNonPAccount(jsonString: String): NonPremiumAccount {
            val gson = Gson()
            val type: Type = object : TypeToken<NonPremiumAccount>() {}.type
            return gson.fromJson(jsonString, type) as NonPremiumAccount
        }


        fun getAllDates(jsonString: String): ArrayList<Int> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<Int?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<Int>
        }

        fun getAllMusic(jsonString: String): ArrayList<MusicFiles> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<MusicFiles?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<MusicFiles>
        }
        fun getAllMemoryFiles(jsonString: String): ArrayList<MemoryBoosterFiles> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<MemoryBoosterFiles?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<MemoryBoosterFiles>
        }
        fun getAllUpgrade(jsonString: String): ArrayList<UpgradeItem> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<UpgradeItem?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<UpgradeItem>
        }

        fun getAllResources(jsonString: String): ArrayList<ResourceFiles> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<ResourceFiles?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<ResourceFiles>
        }

        fun getJsonObject(jsonString: String): JSONObject {
            return JSONObject(jsonString)
        }

        fun getAllDoctorReferrals(jsonString: String): ArrayList<ReferredListItemDoctor> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<ReferredListItemDoctor?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<ReferredListItemDoctor>
        }

        fun getAllPatientReferrals(jsonString: String): ArrayList<ReferredListItemPatient> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<ReferredListItemPatient?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<ReferredListItemPatient>
        }

        fun getAllTicketMessages(jsonString: String): ArrayList<TicketMessage> {
            val gson = Gson()
            val type: Type = object : TypeToken<ArrayList<TicketMessage?>?>() {}.type
            return gson.fromJson(jsonString, type) as ArrayList<TicketMessage>
        }

        fun logApiError(anError: ANError){
            Log.e(
                "apiError",
                "${anError.errorDetail} / ${anError.errorCode} / ${anError.errorBody} / ${anError.response}"
            )
        }

        fun getTimeFromTimeStamp(timeStamp: String): Long{
            val cal = Calendar.getInstance()
            val year = timeStamp.substring(0, 4).toInt()
            val month = timeStamp.substring(5, 7).toInt() - 1
            val date = timeStamp.substring(8, 10).toInt()
            val hour = timeStamp.substring(11, 13).toInt()
            val minute = timeStamp.substring(14, 16).toInt()
            val seconds = timeStamp.substring(17, 19).toInt()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DATE, date)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, seconds)
            cal.timeZone = TimeZone.getTimeZone("UTC")
            return cal.timeInMillis
        }

        fun getUserId(): Int{
            if(sharedPreferences.getBoolean("loggedIn", false))
                return sharedPreferences.getString("uid", "-1")!!.toInt()
            else
                return -1
        }

        fun getDoctorId(): Int{
            if(sharedPreferences.getBoolean("loggedIn", false))
                return sharedPreferences.getString("uid", "-1")!!.toInt()
            else
                return -1
        }

        fun getLanguage():String
        {
            var language="english"
            if(sharedPreferences.contains("language"))
                language= sharedPreferences.getString("language","English")!!.toLowerCase()
            return language
        }

        fun getTimeFromMillis(time: Long): String{
            val cal = Calendar.getInstance()
            cal.timeInMillis = time
            return "" + String.format("%02d", cal.get(Calendar.DATE)) + "/" +
                    String.format("%02d", (cal.get(Calendar.MONTH) + 1)) + " " +
                    String.format("%02d", cal.get(Calendar.HOUR)) + ":"+
                    String.format("%02d", cal.get(Calendar.MINUTE)) + " " +
                    (if(cal.get(Calendar.AM_PM)==Calendar.AM)"am" else "pm")
        }

        fun isEmailValid(email: String?): Boolean {
            val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$"
            val pat: Pattern = Pattern.compile(emailRegex)
            return if (email == null) false else pat.matcher(email).matches()
        }

        fun getUntilNextHour(hourOfDay: Int, calendarInstance: Calendar, inclusive: Boolean = false): Calendar{
            if(hourOfDay<=24) {
                if(inclusive) {
                    if (calendarInstance.get(Calendar.HOUR_OF_DAY) <= hourOfDay) {
                        calendarInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    } else {
                        calendarInstance.add(Calendar.DAY_OF_YEAR, 1)
                        calendarInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    }
                }
                else{
                    if (calendarInstance.get(Calendar.HOUR_OF_DAY) < hourOfDay) {
                        calendarInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    } else {
                        calendarInstance.add(Calendar.DAY_OF_YEAR, 1)
                        calendarInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    }
                }
//                while (calendarInstance.get(Calendar.HOUR_OF_DAY) < hourOfDay) {
//                    calendarInstance.add(Calendar.HOUR_OF_DAY, 1)
//                }
            }
            return calendarInstance
        }

        fun getUntilNextDayOfWeekAfterHour(
            dayOfWeek: Int,
            hourOfDay: Int,
            calendarInstance: Calendar
        ): Calendar {
            if (calendarInstance.get(Calendar.HOUR_OF_DAY) >= hourOfDay && calendarInstance.get(
                    Calendar.DAY_OF_WEEK) == dayOfWeek
            )
                calendarInstance.add(Calendar.DAY_OF_WEEK, 1)
            while (calendarInstance.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                calendarInstance.add(Calendar.DAY_OF_WEEK, 1)
            }
            return calendarInstance
        }

        fun getUntilNextDayOfMonthAfterHour(
            dayOfMonth: Int,
            hourOfDay: Int,
            calendarInstance: Calendar,
            inclusive: Boolean = false
        ): Calendar {
            if (calendarInstance.get(Calendar.HOUR_OF_DAY) >= hourOfDay && calendarInstance.get(
                    Calendar.DAY_OF_MONTH) == dayOfMonth
            )
                calendarInstance.add(Calendar.DAY_OF_MONTH, 1)
            while (calendarInstance.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
                calendarInstance.add(Calendar.DAY_OF_WEEK, 1)
            }
            return calendarInstance
        }

        fun getBabyName(): String {
            return sharedPreferences.getString("babyName", "")!!
        }

        fun getSubscriptionEndingDate(): Int {
            var endingTime=0L
            if (sharedPreferences.contains("uid") && sharedPreferences.getString("uid", "") != null) {
                val userId = sharedPreferences.getString("uid", "")!!

                AndroidNetworking.get("https://www.uptodd.com/api/appusers/checksubscription/{userId}")
                    .addPathParameter("userId", userId)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener
                    {
                        override fun onResponse(response: JSONObject?) {
                            if (response != null && response.get("status") == "Success")
                            {
                                val endingDate=(response.get("data") as JSONObject).getString("subscriptionEndingDate")
                                val calendar=Calendar.getInstance()
                                calendar.set(endingDate.substring(0,4).toInt(),endingDate.substring(5,7).toInt(),endingDate.substring(8,10).toInt())
                                endingTime=calendar.timeInMillis

                                Log.d("div","AllUtil L291 $endingTime")
                            }
                        }

                        override fun onError(error: ANError?) {
                            Log.d("div", "AllUtil L294 $error")
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
//            val calendarInstance = Calendar.getInstance()
//            calendarInstance.add(Calendar.DAY_OF_YEAR, 30)
//            return calendarInstance.get(Calendar.DAY_OF_YEAR)
            Log.d("div","AllUtil L311 $endingTime")
            return endingTime.toInt()
        }

        fun getBabyBirthday(): Long {
            return sharedPreferences.getLong("babyDOB", 0L)
        }

        fun isLoggedInAsDoctor(): Boolean {
            return sharedPreferences.getString("userType", "Normal") == "Doctor"
        }

        fun getMusicImage(song: MusicFiles, dpi: String): String {
            return "https://www.uptodd.com/images/app/android/thumbnails/musics/${dpi}/${song.image!!.trim()}.webp"
        }

        fun getAuthToken():String?{
            return sharedPreferences.getString(com.uptodd.uptoddapp.database.logindetails.UserInfo::tokenHeader.name,"")
        }

        fun getPoemImage(song: MusicFiles, dpi: String): String {
            return if(song.prenatal!=-1)
                "https://www.uptodd.com/images/app/android/details/memory_booster/${song.image!!}.webp"
            else
                "https://www.uptodd.com/images/app/android/thumbnails/poems/${dpi}/${song.image!!.trim()}.webp"
        }

        fun getResourceUrl(name: String):String
        {

            return "https://www.uptodd.com/resources/user/${name}"
        }

        fun getDifferenceDay(start:Long,end:Long):Long
        {
            return TimeUnit.DAYS.convert(end-start,TimeUnit.MILLISECONDS)
        }

        fun getDifferenceMonth(start:Long,end:Long):Long
        {
            return TimeUnit.DAYS.convert(end-start,TimeUnit.MILLISECONDS)/30
        }

        fun isSubscriptionOver(ending:Date):Boolean
        {
            var cal=Calendar.getInstance()
            return cal.time.after(ending)
        }


        fun isSubscriptionOverActive(context: Context,handle:Boolean):Boolean
        {

            val pref=UptoddSharedPreferences.getInstance(context)
            val month=pref.getCurrentPlan()
            val date =LocalDate.parse(pref.getSubEnd())
            Log.d("date",date.toString())

           return when (month) {
               6L -> {
                   false
               }
               3L -> {

                   var added= if (isRow(context))
                       date.plusMonths(12).toString()
                   else
                       date.plusMonths(6).toString()



                   Log.d("added date",added)
                   val end = SimpleDateFormat("yyyy-MM-dd").parse(added)
                   isSubscriptionOver(end)
               }
               else-> {
                   false
               }
           }

        }



        fun isSubscriptionOverActive(context: Context):Boolean
        {

            try {
                val endDate = UptoddSharedPreferences.getInstance(context).getAppExpiryDate()
                val end = SimpleDateFormat("yyyy-MM-dd").parse(endDate)
                return isSubscriptionOver(end)
            }
            catch (e:Exception)
            {
                return isSubscriptionOverActive(context,true)
            }
        }


        fun getAppAccessDate(context: Context):String?
        {
            val pref=UptoddSharedPreferences.getInstance(context)
            val month=pref.getCurrentPlan()
            val date =LocalDate.parse(pref.getSubEnd())
            Log.d("date",date.toString())

            return when (month) {
                6L -> {
                    null
                }
                3L -> {

                    var added= if (isRow(context))
                        date.plusMonths(12).toString()
                    else
                        date.plusMonths(6).toString()



                    Log.d("added date",added)
                    val end = SimpleDateFormat("yyyy-MM-dd").parse(added)
                    added
                }
                else-> {
                    null
                }
            }
        }


        fun isUserPremium(context: Context):Boolean
        {
            return UptoddSharedPreferences.getInstance(context).getUserType()=="premium"
        }

        fun getCountry(context: Context):String
        {
            return  if(UptoddSharedPreferences.getInstance(context).getPhone()?.startsWith("+91")!!)
                "india"
            else
                "row"
        }
        fun isRow(context: Context):Boolean
        {
            return !UptoddSharedPreferences.getInstance(context).getPhone()?.startsWith("+91")!!
        }




    }
    fun s() {
        val cal = Calendar.getInstance()
        val days = cal.getMaximum(Calendar.DAY_OF_MONTH)

        while (cal.get(Calendar.DAY_OF_MONTH) != 1)
            cal.add(Calendar.DAY_OF_MONTH, 1)


    }

}