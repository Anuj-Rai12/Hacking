package com.uptodd.uptoddapp.utilities

import android.content.Context
import android.os.Environment
import android.util.Log
import com.coolerfall.download.*
import java.io.File
import java.util.concurrent.TimeUnit

class Downloader {
    companion object{
        fun startDocumentDownload(context: Context, url: String, fileName: String, downloadCallbackListener: DownloadCallback): Int {
            val manager: DownloadManager = DownloadManager.Builder().context(context)
                .downloader(OkHttpDownloader.create())
                .threadPoolSize(3)
                .logger { message -> Log.d("TAG", message!!) }
                .build()

            val destinationDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Downloads")

            val file = File(destinationDir.path, fileName)
            if(!destinationDir.exists())
                destinationDir.mkdirs()
            if(!destinationDir.canWrite())
                destinationDir.setWritable(true)

            val request: DownloadRequest = DownloadRequest.Builder()
                .url(url)
                .retryTime(3)
                .retryInterval(2, TimeUnit.SECONDS)
                .progressInterval(1, TimeUnit.SECONDS)
                .priority(Priority.HIGH)
                .allowedNetworkTypes(DownloadRequest.NETWORK_WIFI)
                .destinationFilePath(file.path)
                .downloadCallback(downloadCallbackListener)
                .build()

            return manager.add(request)
        }
    }
}