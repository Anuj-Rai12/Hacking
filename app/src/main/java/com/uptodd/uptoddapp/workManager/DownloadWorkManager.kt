package com.uptodd.uptoddapp.workManager

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryBoosterFiles
import com.uptodd.uptoddapp.database.media.memorybooster.MemoryFilesDao
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.music.MusicFilesDatabaseDao
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class DownloadWorkManager(val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private var database: UptoddDatabase?= UptoddDatabase.getInstance(context)
    private  var musicDatabase : MusicFilesDatabaseDao?=database?.musicDatabaseDao
    private  var memoryDatabase : MemoryFilesDao?=database?.memoryBoosterDao
    private  var downloadedMusic: List<MusicFiles>?=null
    private  var downloadedMemoryMusic: List<MemoryBoosterFiles>?=null

    override suspend fun doWork(): Result {
        val manager: DownloadManager = DownloadManager.Builder().context(context)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(3)
            .logger { message -> Log.d("TAG", message!!) }
            .build()

        startMusicDownload(
            File(
                applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "Downloads"
            ),
            manager,context
        )
        return Result.success()
    }

    fun startMusicDownload(destinationDir: File, uptoddDownloadManager: DownloadManager, context: Context) {
        GlobalScope.launch(Dispatchers.IO) {


            val stage= UptoddSharedPreferences.getInstance(context).getStage()
            downloadedMusic = musicDatabase?.getAllFiles()
            downloadedMemoryMusic=memoryDatabase?.getAllFiles()
            Log.i("downloaded", downloadedMusic?.size.toString())
            val language = AllUtil.getLanguage()
            val userType= UptoddSharedPreferences.getInstance(context).getUserType()
            val country= AllUtil.getCountry(context)
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
        mManager: DownloadManager, context: Context
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
            musicDatabase?.insert(music)
        }
    }
    private fun updateMemoryPath(music: MemoryBoosterFiles, path: String) {
        Log.i("inserting", "inserting init")
        GlobalScope.launch(Dispatchers.IO) {
            music.filePath = path
            Log.i("inserting", "${music.name} -> ${music.filePath}")
            memoryDatabase?.insert(music)
        }
    }

    private fun getIsMusicDownloaded(music: MusicFiles): Boolean {
        downloadedMusic?.forEach {
            if (it.id == music.id)
                return@getIsMusicDownloaded true
        }
        return false
    }

    private fun getIsMemoryDownloded(m: MemoryBoosterFiles):Boolean
    {
        downloadedMemoryMusic?.forEach {
            if (it.id == m.id)
                return@getIsMemoryDownloded true
        }
        return false
    }

}