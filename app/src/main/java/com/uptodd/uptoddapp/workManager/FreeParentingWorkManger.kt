package com.uptodd.uptoddapp.workManager

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coolerfall.download.DownloadCallback
import com.coolerfall.download.DownloadManager
import com.coolerfall.download.DownloadRequest
import com.coolerfall.download.OkHttpDownloader
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.setLogCat
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class FreeParentingWorkManger(private val context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    // Already download file in DB
    private val allDownloadFile = mutableListOf<Content>()

    private val videoRepository = VideoContentRepository(
        RetrofitSingleton.getInstance().getRetrofit(),
        UptoddDatabase.getInstance(context).videoContentDao
    )

    private val manager by lazy {
        DownloadManager.Builder().context(context)
            .downloader(OkHttpDownloader.create())
            .threadPoolSize(3)
            .logger { message -> Log.d("TAG", message!!) }
            .build()
    }


    private val findPath by lazy {
        val file = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "Downloads${File.separator}FreeParenting"
        )

        if (!file.exists()) {
            file.mkdirs()
        }
        file
    }

    override suspend fun doWork(): Result {
        return try {
            val flag = true
            if (flag) {
                getVideoContentResponse()
            } else {
                getDeleteDb()
            }
            getVideoContent()
            Result.success()
        } catch (e: Exception) {
            setLogCat("WORK_FREE", e.localizedMessage ?: "Work Error")
            Result.retry()
        }

    }


    private suspend fun downloadFile(
        apiPath: String,
        savePath: String,
        data: Content,
        save: (content: Content) -> Unit
    ) {
        withContext(IO) {
            val item = async {
                val download = DownloadRequest.Builder().url("https://www.uptodd.com$apiPath")
                    .retryTime(3)
                    .retryInterval(2, TimeUnit.SECONDS)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .priority(com.coolerfall.download.Priority.HIGH)
                    .destinationFilePath(savePath)
                    .downloadCallback(object : DownloadCallback {
                        override fun onStart(downloadId: Int, totalBytes: Long) {
                            setLogCat("DOWNLOAD_FREE", "${data.name} download ...")
                        }

                        override fun onRetry(downloadId: Int) {
                            setLogCat("DOWNLOAD_FREE", "${data.name}  ON Retry")
                        }

                        override fun onProgress(
                            downloadId: Int,
                            bytesWritten: Long,
                            totalBytes: Long
                        ) {
                        }

                        override fun onSuccess(downloadId: Int, filePath: String?) {
                            val content = Content(
                                id = data.id,
                                name = data.name,
                                time = data.time,
                                type = data.type,
                                url = savePath
                            )
                            setLogCat("DOWNLOAD_FREE", " ${data.name} downloading Success")
                            save.invoke(content)
                        }

                        override fun onFailure(downloadId: Int, statusCode: Int, errMsg: String?) {
                            setLogCat("DOWNLOAD_FREE", " ${data.name} downloading failed $errMsg")
                        }

                    })
                    .build()
                manager.add(download)
            }
            item.await()
        }
    }


    private suspend fun getDeleteDb() {
        videoRepository.deleteVideoFromDb().collectLatest {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    setLogCat("getDeleteDb", "error -> ${it.exception?.localizedMessage}")
                }
                is ApiResponseWrapper.Loading -> {
                    setLogCat("getDeleteDb", "${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    setLogCat("getDeleteDb", "${it.data}")
                }
            }
        }
    }


    private suspend fun insertFileInDb(content: Content) {
        videoRepository.getInsetVideoFromDb(content).collectLatest {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    setLogCat("insertFileInDb", "error -> ${it.exception?.localizedMessage}")
                }
                is ApiResponseWrapper.Loading -> {
                    setLogCat("insertFileInDb", "${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    setLogCat("insertFileInDb", "${it.data}")
                }
            }
        }
    }


    private suspend fun getVideoContent() {
        videoRepository.getVideoContent().collectLatest {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    setLogCat("Video_Fetch", "getVideoContent: ${it.exception?.localizedMessage}")
                }
                is ApiResponseWrapper.Loading -> {
                    setLogCat("Video_Fetch", "getVideoContent: ${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    setLogCat("Video_Fetch", "getVideoContent: ${it.data}")
                    (it.data as VideoContentList?)?.let { videoContentList ->
                        val newContent = getVideoContentFromList(videoContentList)
                        downloadVideoFile(newContent)
                    }
                }
            }
        }
    }

    private suspend fun downloadVideoFile(newContent: MutableList<Content>) {
        val indexList = mutableListOf<Content>()
        newContent.forEach { content ->
            if (!isItemIsDownloadOrNot(content = content)) {
                indexList.add(content)
                /*val fileName =
                    content.name.trim().uppercase(Locale.getDefault()).replace("\\s".toRegex(), "")
                downloadFile(content.url, File(findPath, "$fileName.acc").absolutePath, content)*/
            }
        }

        if (indexList.isNotEmpty()) {
            indexList.forEach { content ->
                val fileName =
                    content.name.trim().uppercase(Locale.getDefault()).replace("\\s".toRegex(), "")

                downloadFile(
                    content.url,
                    File(findPath, "$fileName.acc").absolutePath,
                    content
                ) { downloadFile ->
                    runBlocking(IO) {
                        setLogCat("INSERT_DB", "Download Content to save $downloadFile")
                        insertFileInDb(downloadFile)
                    }
                }
            }
        } else {
            setLogCat("INSERT_DB", "Nothing is Saved")
        }
    }


    private fun isItemIsDownloadOrNot(content: Content): Boolean {
        if (allDownloadFile.isEmpty()) {
            return false//Not in DB
        }
        allDownloadFile.forEach { items ->
            if (content.id == items.id && items.name == content.name) {
                return true
            }
        }
        return false
    }


    @Suppress("UNCHECKED_CAST")
    private suspend fun getVideoContentResponse() {
        videoRepository.getAllVideoFromDb().collectLatest {
            when (it) {
                is ApiResponseWrapper.Error -> {
                    setLogCat("DB_Response", "${it.exception?.localizedMessage}")
                }
                is ApiResponseWrapper.Loading -> {
                    setLogCat("DB_Response", "${it.data}")
                }
                is ApiResponseWrapper.Success -> {
                    setLogCat("DB_Response", "video from database -> ${it.data}")
                    if (it.data !is String) {
                        val item = it.data as List<Content>
                        allDownloadFile.addAll(item)
                    }
                }
            }
        }
    }


    private fun getVideoContentFromList(videoContentList: VideoContentList): MutableList<Content> {
        val mutableList = mutableListOf<Content>()
        videoContentList.data.forEach { data ->
            val item = data.content.filter { content ->
                content.type == VideoContentRepository.Companion.ItemType.MUSIC.name
            }
            mutableList.addAll(item)
        }
        return mutableList
    }


}