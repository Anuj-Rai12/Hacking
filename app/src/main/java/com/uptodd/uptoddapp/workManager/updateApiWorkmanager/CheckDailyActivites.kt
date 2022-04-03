package com.uptodd.uptoddapp.workManager.updateApiWorkmanager

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
import com.uptodd.uptoddapp.SplashScreenActivity
import com.uptodd.uptoddapp.alarmsAndNotifications.receivers.NotificationBroadcastReceiver
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.api.getPeriod
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryBoosterFiles
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryFilesDao
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.DAILY_ALARM_REQUEST_CODE
import com.uptodd.uptoddapp.utilities.UptoddNotificationUtilities
import com.uptodd.uptoddapp.utilities.UptoddNotify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class CheckDailyActivites(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters)  {
    override suspend fun doWork(): Result = coroutineScope {

        checkSessionAdded(context)
        checkMemoryBoosterAdded(context)
        checkPodcastAdded(context)
        Result.success()
    }

    private fun checkPodcastAdded(context: Context)
    {
        val uid = AllUtil.getUserId()
        val months= getMonth(context!!)
        val lang= AllUtil.getLanguage()
        val country= AllUtil.getCountry(context)
        val stage=UptoddSharedPreferences.getInstance(context).getStage()
        val userType= UptoddSharedPreferences.getInstance(context).getUserType()
        val size= UptoddSharedPreferences.getInstance(context).getSaveCountPodcast()

        AndroidNetworking.get("https://www.uptodd.com/api/activitypodcast?userId={userId}&months={months}&lang={lang}&userType=$userType&country=$country&motherStage=$stage")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("months", months.toString())
            .addPathParameter("lang",lang)
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return

                    try {

                        val data = response.get("data") as JSONArray

                        Log.d("size p","${data.length()} > $size")

                        if(data.length()>size)
                        {
                            showNotification(
                                context,"New Podcast Added",
                                "Hey Mom/Dad, Check new Podcast Added for you.",
                                "Podcast",
                                60009,
                                NotificationCompat.PRIORITY_DEFAULT
                            )
                            context.getSharedPreferences("last_updated", Context.MODE_PRIVATE).edit().putLong("ACTIVITY_PODCAST",-1).apply()
                        }

                    }
                    catch (exception:Exception)
                    {

                    }
                }


                override fun onError(anError: ANError?) {
                    ListenableWorker.Result.retry()
                }

            })
    }

    private fun checkMemoryBoosterAdded(context: Context)
    {

        val uid = AllUtil.getUserId()
        val stage= UptoddSharedPreferences.getInstance(context).getStage()
        val prenatal =if(stage=="pre birth" || stage=="prenatal")  0 else 1
        val lang = AllUtil.getLanguage()
        val country= AllUtil.getCountry(context)
        val userType=UptoddSharedPreferences.getInstance(context).getUserType()
        val size= UptoddSharedPreferences.getInstance(context).getSaveCountMemory()
        val database= UptoddDatabase.getInstance(context).memoryBoosterDao
        val manager: DownloadManager = DownloadManager.Builder().context(context)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(3)
            .logger { message -> Log.d("TAG", message!!) }
            .build()
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

                        val poems = AllUtil.getAllMemoryFiles(response.get("data").toString())
                        Log.d("size m","${poems.size} > $size")
                        if(poems?.size>size)
                        {
                            showNotification(
                                context,"New Memory  Booster Music  Added",
                                "Hey Mom/Dad, Check new Memory Booster Music Added for you.",
                                "MemoryBooster",
                                60007,
                                NotificationCompat.PRIORITY_DEFAULT

                                )




                            GlobalScope.launch {

                                poems.forEach {
                                    if (getIsMemoryDownloaded(database.getAllFiles(), it))
                                        it.filePath = database.getFilePath(it.id)
                                    else {

                                        val destinationDir = File(
                                            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                                            "Downloads"
                                        )

                                        val destDir = File(destinationDir, "music")
                                        downloadMusicFile(database, it, destinationDir, manager)
                                    }
                                }

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
    fun getIsMemoryDownloaded(downloadedPoems:List<MemoryBoosterFiles>,poem: MemoryBoosterFiles): Boolean {
        downloadedPoems.forEach {
            if (it.id == poem.id)
                return@getIsMemoryDownloaded true
        }
        return false
    }
    fun downloadMusicFile(musicDatabase: MemoryFilesDao,
                          fileMusic: MemoryBoosterFiles,
                          destinationDir: File,
                          mManager: DownloadManager,
    ) {
        val file = File(destinationDir.path, "${fileMusic.file}.aac")
        if (file.exists())
            file.delete()
        if (!file.exists())
            destinationDir.mkdirs()
        if (!destinationDir.canWrite())
            destinationDir.setWritable(true)

        val destinationUri = Uri.fromFile(file)
        Log.i(
            "inserting",
            "starting -> ${fileMusic.file}. Downloading from : https://uptodd.com/files/memory_booster/${fileMusic.file?.trim()}.aac"
        )

        val request: DownloadRequest = DownloadRequest.Builder()
            .url("https://www.uptodd.com/files/memory_booster/${fileMusic.file?.trim()}.aac")
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
                        "on start -> ${fileMusic.name}"
                    )
                }

                override fun onRetry(downloadId: Int) {}
                override fun onProgress(
                    downloadId: Int,
                    bytesWritten: Long,
                    totalBytes: Long,
                ) {
                }

                override fun onSuccess(downloadId: Int, filePath: String) {
                    updatePath(musicDatabase,fileMusic,file.path)
                    Log.i("inserting", "on success")
                }

                override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String) {
                    Log.i(
                        "inserting",
                        "on failed -> ${fileMusic.file}. Cause: $errMsg"
                    )
                }
            })
            .build()

        val downloadId: Int = mManager.add(request)
    }


    private fun isMusic(song: MusicFiles): Boolean {
        return song.language == null
    }


    private fun updatePath(musicDatabase: MemoryFilesDao, music: MemoryBoosterFiles, path: String) {
        Log.i("inserting", "inserting init")
        GlobalScope.launch {
            music.filePath = path
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            musicDatabase.insert(music)
        }
    }


    private fun checkSessionAdded(context: Context)
    {
        val period = getPeriod(context)
        val uid = AllUtil.getUserId()
        val userType= UptoddSharedPreferences.getInstance(context).getUserType()
        val country= AllUtil.getCountry(context)
        val size= UptoddSharedPreferences.getInstance(context).getSaveCountSession()

        AndroidNetworking.get("https://www.uptodd.com/api/activitysample?userId={userId}&period={period}&userType=$userType&country=$country")
            .addPathParameter("userId", uid.toString())
            .addPathParameter("period", period.toString())
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response == null) return
                    try {
                        val data = response.get("data") as JSONArray

                        if(data.length()>size)
                        {
                            showNotification(
                                context,"New Session Added",
                                "Hey Mom/Dad, Check new Session Added for you.",
                                "ActivitySample",
                                NotificationCompat.PRIORITY_DEFAULT,
                                60008
                            )
                            context.getSharedPreferences("last_updated", Context.MODE_PRIVATE).edit().putLong("ACTIVITY_SAMPLE",-1).apply()

                        }

                    } catch (e: Exception) {

                        return
                    } finally {

                    }
                }


                override fun onError(anError: ANError?) {

                }

            })
    }

    companion object
    {
     fun showNotification(context: Context,title:String,text:String,notificationChannelId:String,notificationId:Int,priority:Int)
    {
        val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Podcast", "PodcastNotification", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MemoryBooster",
                "MemoryBoosterNotification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ActivitySample",
                "ActivitySampleNotification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "new channel"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, SplashScreenActivity::class.java)
        notificationIntent?.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
        notificationIntent.putExtra("notifyId",notificationChannelId)

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
        }






}