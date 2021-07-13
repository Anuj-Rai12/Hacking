package com.uptodd.uptoddapp.alarmsAndNotifications.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
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
import com.uptodd.uptoddapp.api.getMonth
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.*
import com.uptodd.uptoddapp.utilities.AppNetworkStatus.Companion.context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra("hasActivityIntent", false)) {
            val bundle = Bundle()
            bundle.putString("activityIntent", intent.getStringExtra("activityIntent"))
        }
        if(intent.getStringExtra("type")=="Podcast")
            checkPodcastAdded(context,intent)
        else if(intent.getStringExtra("type")=="MemoryBooster")
        checkMemoryBoosterAdded(context,intent)
        else
            showNotification(context,intent)
    }

   private fun showNotification(context: Context,intent: Intent)
    {
        val notificationIntent = Intent(context, SplashScreenActivity::class.java)

        val builder = UptoddNotificationUtilities.notificationBuilder(
            context,
            intent.getStringExtra("notificationTitle")!!,
            intent.getStringExtra("notificationText")!!,
            notificationIntent,
            intent.getStringExtra("notificationChannelId")!!,
            intent.getIntExtra("notificationPriority", NotificationCompat.PRIORITY_DEFAULT)
        )

        val notificationId = intent.getIntExtra("notificationId", DEFAULT_NOTIFICATION_ID)


        NotificationManagerCompat.from(context).UptoddNotify(
            builder,
            notificationId
        )

    }

    fun checkPodcastAdded(context: Context,intent: Intent)
    {
        val uid = AllUtil.getUserId()
        val months= getMonth(context!!)
        val lang= AllUtil.getLanguage()

        val size=UptoddDatabase.getInstance(context).activityPodcastDao.getAll().value?.size

        AndroidNetworking.get("https://uptodd.com/api/activitypodcast?userId={userId}&months={months}&lang={lang}")
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

                        if(data.length()>size!!)
                        {
                           showNotification(context,intent)
                            context.getSharedPreferences("last_updated", Context.MODE_PRIVATE).edit().putLong("last_checked",-1).apply()
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

    fun getIsPoemDownloaded(downloadedPoems:List<MusicFiles>,poem: MusicFiles): Boolean {
        downloadedPoems.forEach {
            if (it.id == poem.id)
                return@getIsPoemDownloaded true
        }
        return false
    }

    fun checkMemoryBoosterAdded(context: Context,intent: Intent)
    {

        val manager: DownloadManager = DownloadManager.Builder().context(context)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(3)
            .logger { message -> Log.d("TAG", message!!) }
            .build()

        val database=UptoddDatabase.getInstance(context).musicDatabaseDao
        val uid = AllUtil.getUserId()
        val prenatal =if(UptoddSharedPreferences.getInstance(context).getStage()=="pre birth") 0 else 1
        val lang = AllUtil.getLanguage()
        AndroidNetworking.get("https://uptodd.com/api/memorybooster?userId={userId}&prenatal={prenatal}&lang={lang}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .addPathParameter("userId",uid.toString())
            .addPathParameter("prenatal",prenatal.toString())
            .addPathParameter("lang",lang)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {

                        GlobalScope.launch {
                            val poems = AllUtil.getAllMusic(response.get("data").toString())

                            if(poems.size>database.getAllSpeedBoosterFiles().size) {

                                showNotification(context,intent)
                                poems.forEach {
                                    if (getIsPoemDownloaded(database.getAllDownloadedMusic(), it))
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

    fun downloadMusicFile(musicDatabase: MusicFilesDatabaseDao,
        fileMusic: MusicFiles,
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
                    .url("https://uptodd.com/files/memory_booster/${fileMusic.file?.trim()}.aac")
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

    private fun updatePath(musicDatabase: MusicFilesDatabaseDao,music: MusicFiles, path: String) {
        Log.i("inserting", "inserting init")
        GlobalScope.launch {
            music.filePath = path
            if (isMusic(music))
                music.language = "NA"
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            musicDatabase.insert(music)
        }
    }

}