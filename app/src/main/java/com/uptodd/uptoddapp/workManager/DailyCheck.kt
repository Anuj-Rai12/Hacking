package com.uptodd.uptoddapp.workManager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.uptodd.uptoddapp.R
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.alarmsAndNotifications.UptoddNotifications
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.DailyNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.EssentialsNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.MonthlyNotificationsReceiver
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.activity.WeeklyNotificationsReceiver
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryBoosterFiles
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class DailyCheck(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val  database = UptoddDatabase.getInstance(context)
    private val musicDatabase = database.musicDatabaseDao
    private  val memoryDatabase=database.memoryBoosterDao
    private lateinit var downloadedMusic: List<MusicFiles>
    private lateinit var downloadedMemoryMusic: List<MemoryBoosterFiles>


    private fun checkDailyActivityStatus() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(0, cal)
        cal.set(Calendar.MINUTE, 0)
        UptoddNotifications.checkDailyActivityStatus(context, cal.timeInMillis)
    }

    private fun setReferralNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SUNDAY, 20, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 20)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotifications.setWeeklyReferNotification(context, calendarInstance.timeInMillis)

    }


    private fun setPhotoNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SUNDAY, 18, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 18)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotifications.setWeeklyPhotoNotification(context, calendarInstance.timeInMillis)

    }


    private fun setEssentialActivityNotification() {
        var calendarInstance = Calendar.getInstance()

        calendarInstance = AllUtil.getUntilNextHour(12, calendarInstance)

        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            ESSENTIALS_ALARM_REQUEST_CODE,
            EssentialsNotificationsReceiver::class.java
        )
    }

    private fun setMonthlyActivityNotification() {
        var date=context.getSharedPreferences("LOGIN_INFO",Context.MODE_PRIVATE)
            .getInt("loginDate",1)

        var calendarInstance = Calendar.getInstance()


        calendarInstance = AllUtil.getUntilNextDayOfMonthAfterHour(date, 20, calendarInstance)

        calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            MONTHLY_ALARM_REQUEST_CODE,
            MonthlyNotificationsReceiver::class.java
        )

        when{
            date<=13 -> date+=15
            date>=16 -> date-=15
            else -> date=1
        }

        calendarInstance = AllUtil.getUntilNextDayOfMonthAfterHour(date, 20, calendarInstance)

        calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            MONTHLY_ALARM_REQUEST_CODE,
            MonthlyNotificationsReceiver::class.java
        )
    }

    private fun setWeeklyActivityNotification() {
        val dayOfWeek=context.getSharedPreferences("LOGIN_INFO",Context.MODE_PRIVATE)
            .getInt("loginDay",1)

        var calendarInstance = Calendar.getInstance()

        calendarInstance =
            AllUtil.getUntilNextDayOfWeekAfterHour(dayOfWeek, 20, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 20)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        UptoddNotificationUtilities.setAlarm(
            context,
            calendarInstance.timeInMillis,
            WEEKLY_ALARM_REQUEST_CODE,
            WeeklyNotificationsReceiver::class.java
        )
    }

    private fun checkSubscription() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(0, cal)
        cal.set(Calendar.MINUTE, 0)
        UptoddNotifications.setSubscriptionEndingNotification(context, cal.timeInMillis)
    }

    private fun setBirthdayWish() {
        val birthdayTime = AllUtil.getBabyBirthday()
        val birthdayCalendarInstance = Calendar.getInstance()
        birthdayCalendarInstance.timeInMillis = birthdayTime

        var calendarInstance = Calendar.getInstance()

        calendarInstance =
            AllUtil.getUntilNextDayOfMonthAfterHour(birthdayCalendarInstance.get(Calendar.DAY_OF_MONTH),
                0,
                calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 0)
        calendarInstance.set(Calendar.MINUTE, 0)

        val dayDifference =
            calendarInstance.get(Calendar.DAY_OF_YEAR) - birthdayCalendarInstance.get(Calendar.DAY_OF_YEAR)
        val noOfMonths = dayDifference / 30
        var text = ""

        val months = KidsPeriod(context).getKidsAge()
        if (months >= 12)
            text = "${months / 12} years, ${months % 12} months"
        else
            text = "$months months"
        UptoddNotifications.setWishNotification(context, calendarInstance.timeInMillis, text)
    }


    override suspend fun doWork(): Result = coroutineScope {

        if (!AllUtil.isLoggedInAsDoctor()) {
            setBirthdayWish()
            checkSubscription()
            checkDailyActivityStatus()
            setEssentialActivityNotification()
            setReferralNotification()
            setPhotoNotification()
            setDailyActivityCheck()
            setWeeklyActivityNotification()
            setMonthlyActivityNotification()

            if(AllUtil.isUserPremium(context))
            {
                checkAppAccess()
            }

            //setSessionLeftReminder()

        } else {
            //TODO: add doctor notifications
            setWeeklyDoctorReferralNotification()
            setDailyReferralReminderCheck()
        }

        Result.success()
    }








    private fun setSessionLeftReminder() {
        var calendarInstance = Calendar.getInstance()

        val lengthOfMonth = calendarInstance.getActualMaximum(Calendar.MONTH)

        calendarInstance.set(Calendar.DAY_OF_MONTH, lengthOfMonth-5)

        if(calendarInstance.timeInMillis>System.currentTimeMillis()) {

            calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance, true)
            calendarInstance.set(Calendar.MINUTE, 0)
            calendarInstance.set(Calendar.SECOND, 0)

            UptoddNotificationUtilities.setAlarm(
                context,
                calendarInstance.timeInMillis,
                MONTHLY_SESSIONS_CHECK_ALARM_REQUEST_CODE,
                MonthlyNotificationsReceiver::class.java
            )
        }
        else{
            calendarInstance.add(Calendar.DAY_OF_MONTH, 3)
            calendarInstance = AllUtil.getUntilNextHour(20, calendarInstance, true)
            calendarInstance.set(Calendar.MINUTE, 0)
            calendarInstance.set(Calendar.SECOND, 0)

            UptoddNotificationUtilities.setAlarm(
               context,
                calendarInstance.timeInMillis,
                MONTHLY_SESSIONS_CHECK_ALARM_REQUEST_CODE,
                MonthlyNotificationsReceiver::class.java
            )
        }
    }

    private fun setDailyReferralReminderCheck() {
        val cal = Calendar.getInstance()
        val referralTimeCal = Calendar.getInstance()
        val pref = context.getSharedPreferences("REFERRAL", Context.MODE_PRIVATE)
        val firstReferral = pref.getBoolean("firstReferral", true)
        if(firstReferral){
            var notificationTime = Calendar.getInstance()
            notificationTime = AllUtil.getUntilNextHour(12, notificationTime)
            notificationTime.set(Calendar.MINUTE, 0)
            UptoddNotifications.setDoctorFirstReferralReminder(notificationTime.timeInMillis, context)
        }
        else {
            referralTimeCal.timeInMillis = pref.getLong("latestReferral", 0)
            val days = cal.get(Calendar.DAY_OF_YEAR) - referralTimeCal.get(Calendar.DAY_OF_YEAR)
            if (days > 3) {
                var notificationTime = Calendar.getInstance()
                notificationTime = AllUtil.getUntilNextHour(10, notificationTime)
                notificationTime.set(Calendar.MINUTE, 0)
                UptoddNotifications.setDoctorDailyReferralReminderCheck(days,
                    notificationTime.timeInMillis,
                    context)
            }
        }
    }

    private fun setWeeklyDoctorReferralNotification() {
        var calendarInstance = Calendar.getInstance()
        calendarInstance = AllUtil.getUntilNextDayOfWeekAfterHour(Calendar.SATURDAY, 12, calendarInstance)
        calendarInstance.set(Calendar.HOUR_OF_DAY, 12)
        calendarInstance.set(Calendar.MINUTE, 0)
        calendarInstance.set(Calendar.SECOND, 0)

        val stringIntentExtras = HashMap<String, String>()
        stringIntentExtras["notificationTitle"] = "Refer Doctors"
        stringIntentExtras["notificationText"] = "Refer to your doctor friends to earn 10% flat."
        stringIntentExtras["notificationChannelId"] = "others"

        val intIntentExtras = HashMap<String, Int>()
        intIntentExtras["notificationPriority"] = NotificationCompat.PRIORITY_DEFAULT
        intIntentExtras["notificationId"] = WEEKLY_DOCTOR_REFER_ALARM

        UptoddNotificationUtilities.setAlarm(context,
            calendarInstance.timeInMillis,
            WEEKLY_REFER_ALARM_REQUEST_CODE,
            broadcastReceiver = NotificationBroadcastReceiver::class.java,
            intentIntegerExtras = intIntentExtras,
            intentStringExtras = stringIntentExtras)
    }

    private fun setDailyActivityCheck() {
        var cal = Calendar.getInstance()
        cal = AllUtil.getUntilNextHour(20, cal)
        cal.set(Calendar.MINUTE, 0)

        UptoddNotificationUtilities.setAlarm(context,
            cal.timeInMillis,
            DAILY_ALARM_REQUEST_CODE,
            DailyNotificationsReceiver::class.java)
    }


    private fun  checkPersonalisedExpiry()
    { val daysLeft=UptoddSharedPreferences.getInstance(context).daysLeftP()
        Log.d("Personal expiry","$daysLeft")
        if(daysLeft==7L || daysLeft==1L)
        {
            val endDate=UptoddSharedPreferences.getInstance(context).getSubEnd()
            showNotification(
                context,"Personalised Subscription expiry",
                "Your Personalised subscription will expire on $endDate",
                "ExNotify",
                60012,
                NotificationCompat.PRIORITY_DEFAULT
            )
        }
        else if(daysLeft==0L)
        {
            showNotification(
                context,"Personalised Subscription expiry",
                "Your Personalised subscription expired",
                "ExNotify",
                60012,
                NotificationCompat.PRIORITY_DEFAULT
            )
        }
    }
    private fun checkAppAccess()
    {
        val plan=UptoddSharedPreferences.getInstance(context).getCurrentPlan()
        if(plan==3L)
        {
            val daysLeft=UptoddSharedPreferences.getInstance(context).daysLeftA()

            Log.d("App Access expiry","$daysLeft")
            if(daysLeft!=-1L)
            {

                if(daysLeft==7L || daysLeft==1L)
                {
                    val endDate=UptoddSharedPreferences.getInstance(context).getAppExpiryDate()
                    showNotification(
                        context,"Subscription expiry",
                        "Your subscription will expire on $endDate",
                        "ExNotify",
                        60012,
                        NotificationCompat.PRIORITY_DEFAULT
                    )
                }
                else if(daysLeft==0L)
                {
                    showNotification(
                        context,"Subscription expiry",
                        "Your subscription expired",
                        "ExNotify",
                        60012,
                        NotificationCompat.PRIORITY_DEFAULT
                    )
                }







            }
        }
    }
    private fun showNotification(context: Context,title:String,text:String,notificationChannelId:String,notificationId:Int,priority:Int)
    {
        val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("ExNotify", "ExpiryNotification", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(context, SplashScreenActivity::class.java)
        val builder = UptoddNotificationUtilities.notificationBuilder(
            context,
            title,
            text,
            notificationIntent,
            notificationChannelId,
            priority
        )

        NotificationManagerCompat.from(context).UptoddNotify(
            builder,
            notificationId
        )

    }


    fun startMusicDownload(destinationDir: File, uptoddDownloadManager: DownloadManager,context: Context) {
        GlobalScope.launch(Dispatchers.IO) {


            val stage=UptoddSharedPreferences.getInstance(context).getStage()
            downloadedMusic = musicDatabase.getAllFiles()
            downloadedMemoryMusic=memoryDatabase.getAllFiles()
            Log.i("downloaded", downloadedMusic.size.toString())
            val language = AllUtil.getLanguage()
            val userType=UptoddSharedPreferences.getInstance(context).getUserType()
            val country=AllUtil.getCountry(context)
            AndroidNetworking.get("https://www.uptodd.com/api/musics?lang=$language&userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {

                            UptoddSharedPreferences.getInstance(context).saveMDownStatus(true)
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val apiFiles = AllUtil.getAllMusic(response.get("data").toString())
                                    val destDir = File(destinationDir, "music")
                                    downloadMusicFiles(apiFiles, destDir, uptoddDownloadManager,context)
                                }
                                catch (e:Exception)
                                {

                                }
                            }
                        }
                    }

                    override fun onError(error: ANError) {
                    }
                })


            AndroidNetworking.get("https://www.uptodd.com/api/poems?userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val poems = AllUtil.getAllMusic(response.get("data").toString())
                                    val destDir = File(destinationDir, "poem")
                                    downloadPoemFiles(poems, destDir, uptoddDownloadManager)
                                }
                                catch (e:java.lang.Exception)
                                {

                                }
                            }
                        }
                    }

                    override fun onError(error: ANError) {}
                })

            val uid = AllUtil.getUserId()
            val prenatal =if(stage=="pre birth" || stage=="prenatal")  0 else 1
            val lang = AllUtil.getLanguage()
            AndroidNetworking.get("https://www.uptodd.com/api/memorybooster?userId={userId}&prenatal={prenatal}&lang={lang}&userType=$userType&country=$country&motherStage=$stage")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addPathParameter("userId",uid.toString())
                .addPathParameter("prenatal",prenatal.toString())
                .addPathParameter("lang",lang)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {
                            GlobalScope.launch(Dispatchers.IO) {
                                try {
                                    val speedBooster =
                                        AllUtil.getAllMemoryFiles(response.get("data").toString())
                                    val destDir = File(destinationDir, "speedbooster")
                                    downloadSpeedBoosterFiles(
                                        speedBooster,
                                        destDir,
                                        uptoddDownloadManager
                                    )
                                }
                                catch (e:java.lang.Exception)
                                {

                                }
                            }
                        } else {

                        }
                    }

                    override fun onError(error: ANError) {

                        Log.i("error", error.errorBody)
                    }
                })




        }

    }

    fun downloadPoemFiles(
        poems: List<MusicFiles>,
        destinationDir: File,
        mManager: DownloadManager
    ) {
        poems.forEach { poem ->
            if (!getIsMusicDownloaded(poem)) {
                val file = File(destinationDir.path, "${poem.file}.aac")
                if (file.exists())
                {
                    updatePath(poem, file.path)
                    return@downloadPoemFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

//                val destinationUri = Uri.fromFile(file)

                Log.i(
                    "inserting",
                    "starting -> ${poem.name}. Downloading from: https://www.uptodd.com/files/poem/${poem.file!!.trim()}.aac"
                )

                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/poem/${poem.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -? ${poem.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            updatePath(poem, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${poem.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)
            }
        }
    }


    private fun downloadMusicFiles(
        files: List<MusicFiles>,
        destinationDir: File,
        mManager: DownloadManager,context: Context
    ) {
        files.forEach {
            if (!getIsMusicDownloaded(it)) {

                val file = File(destinationDir.path, "${it.file}.aac")
                if (file.exists())
                {
                    updatePath(it, file.path)
                    return@downloadMusicFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

                val destinationUri = Uri.fromFile(file)
                Log.i(
                    "inserting",
                    "starting -> ${it.name}. Downloading from : https://www.uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac"
                )

                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/music/${it.image!!.trim()}/${it.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -> ${it.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            if(files.last().id==it.id)
                            {
                                UptoddSharedPreferences.getInstance(context).saveMDownStatus(false)
                            }
                            updatePath(it, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${it.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)

            }
        }
    }

    fun downloadSpeedBoosterFiles(
        files: List<MemoryBoosterFiles>,
        destinationDir: File,
        mManager: DownloadManager
    ) {
        files.forEach {
            if (!getIsMemoryDownloded(it)) {
                val file = File(destinationDir.path, "${it.file}.aac")
                if (file.exists())
                {
                    updateMemoryPath(it, file.path)
                    return@downloadSpeedBoosterFiles
                }
                if (!file.exists())
                    destinationDir.mkdirs()
                if (!destinationDir.canWrite())
                    destinationDir.setWritable(true)

                val destinationUri = Uri.fromFile(file)
                Log.i(
                    "inserting",
                    "starting -> ${it.name}. Downloading from : https://www.uptodd.com/files/memory_booster/${it.file!!.trim()}.aac"
                )


                val request: DownloadRequest = DownloadRequest.Builder()
                    .url("https://www.uptodd.com/files/memory_booster/${it.file!!.trim()}.aac")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    //.allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                    .destinationFilePath(file.path)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            Log.i(
                                "inserting",
                                "on start -> ${it.name}"
                            )
                        }

                        override fun onRetry(downloadId: Int) {}
                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String) {
                            updateMemoryPath(it, file.path)
                            Log.i("inserting", "on success")
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                            Log.i(
                                "inserting",
                                "on failed -> ${it.name}. Cause: $errMsg"
                            )
                        }
                    })
                    .build()

                val downloadId: Int = mManager.add(request)
            }
        }
    }






    private fun isMusic(song: MusicFiles): Boolean {
        return song.language == null
    }

    private fun updatePath(music: MusicFiles, path: String) {
        Log.i("inserting", "inserting init")
        GlobalScope.launch(Dispatchers.IO){

            music.filePath = path
            if (isMusic(music))
                music.language = "NA"
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            musicDatabase.insert(music)
        }
    }
    private fun updateMemoryPath(music: MemoryBoosterFiles, path: String) {
        Log.i("inserting", "inserting init")
        GlobalScope.launch(Dispatchers.IO) {
            music.filePath = path
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            memoryDatabase.insert(music)
        }
    }

    private fun getIsMusicDownloaded(music: MusicFiles): Boolean {
        downloadedMusic.forEach {
            if (it.id == music.id)
                return@getIsMusicDownloaded true
        }
        return false
    }


    private fun getIsMemoryDownloded(m: MemoryBoosterFiles):Boolean
    {
        downloadedMemoryMusic.forEach {
            if (it.id == m.id)
                return@getIsMemoryDownloded true
        }
        return false
    }


}