package  com.uptodd.uptoddapp.utilities.downloadmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.NonNull
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class UpToddDownloadManager constructor(
    private val context: Context
) {

    private lateinit var url: String

    private lateinit var destinationUri: Uri

    private  val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private var downloadListener: DownloadListener? = null
    private val downloadHandler by lazy {
        DownloadHandler(downloadListener)
    }
    private val scheduledExecutorService by lazy {
        Executors.newSingleThreadScheduledExecutor()
    }

    private val contentObserver by lazy {
        DownloadChangeObserver()
    }
    private  var downloadId :  Long  =  0
    private var scheduledFuture: ScheduledFuture<*>? = null

    /**
     * @param url download address
     */
    fun  setUrl ( @NonNull  downloadURL :  String ) :  UpToddDownloadManager {
        this.url = downloadURL
        return this
    }

    fun setDestinationUri(destinationUri: Uri){
        this.destinationUri= destinationUri
    }

    /**
     * @param downloadListener download listener
     */
    fun setListener(downloadListener: DownloadListener?): UpToddDownloadManager {
        this.downloadListener = downloadListener
        return this
    }

    /**
     * start download
     */
    fun download() {
        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE).apply {
                addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
            })
        val request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationUri(destinationUri)
        downloadListener?.onPrepare()
        val uri = Uri.parse("content://downloads/all_downloads") // 标识/Download
        context.contentResolver.registerContentObserver(uri, false, contentObserver)
        downloadId = downloadManager.enqueue (request)
    }

    /**
     * Cancel download
     */
    fun  cancel () {
        if (downloadId != 0L)
            downloadManager.remove (downloadId)
        unSubscribe()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra ( DownloadManager . EXTRA_DOWNLOAD_ID , - 1 )
            when (intent.action) {
                DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                    if (downloadId == -1L) {
                        unSubscribe()
                        return
                    }

                    val uri = downloadManager.getUriForDownloadedFile (downloadId)
                    if (uri != null) {
                        downloadHandler.apply {
                            sendMessage(obtainMessage(HANDLE_DOWNLOAD, 1, 1))
                        }
                        downloadListener?.onSuccess(destinationUri.path!!)
                    } else
                        downloadListener?.onFailed( Exception ( " download failed, id=$downloadId " ))
                    //After the download is successful, the subscription is delayed
                    downloadHandler.postDelayed({
                        unSubscribe()
                    }, 500)
                }
                DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                    downloadListener?.onNotificationClicked()
                }
            }
        }
    }

    /**
     * Monitor download progress
     */
    private inner class DownloadChangeObserver : ContentObserver(downloadHandler) {
        /**
         * When the monitored Uri changes, this method will be called back
         * @param selfChange
         */
        override fun onChange(selfChange: Boolean) {
            Log.d(TAG, "on change:$selfChange")
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
                {
                    val bytesAndStatus = getBytesAndStatus (downloadId)
                    downloadHandler.sendMessage(
                        downloadHandler.obtainMessage(
                            HANDLE_DOWNLOAD,
                            bytesAndStatus [ 0 ],
                            bytesAndStatus [ 1 ],
                            bytesAndStatus [ 2 ]
                        )
                    )
                },
                0,
                1,
                TimeUnit.SECONDS
            ) // Query in child thread
        }
    }

    /**
     * Query the download status through query, including the downloaded data size, total size, and download status
     *
     * @param downloadId download task id
     * @return current task status pos[0]: downloaded bytes pos[1]: total downloaded bytes pos[2]: current download status
     */
    private  fun  getBytesAndStatus ( downloadId :  Long ) :  IntArray {
        val bytesAndStatus = intArrayOf ( - 1 , - 1 , 0 )
        val query =  DownloadManager.Query () .setFilterById (downloadId)
        var cursor: Cursor? = null
        try {
            cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                bytesAndStatus [ 0 ] =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                bytesAndStatus [ 1 ] =
                    cursor.getInt (cursor.getColumnIndexOrThrow ( DownloadManager . COLUMN_TOTAL_SIZE_BYTES ))
                bytesAndStatus [ 2 ] =
                    cursor.getInt (cursor.getColumnIndex ( DownloadManager . COLUMN_STATUS ))
            }
        } finally {
            cursor?.close()
        }
        return bytesAndStatus
    }

    /**
     * Turn off timers, threads and other operations
     * After receiving the ACTION_DOWNLOAD_COMPLETE broadcast, unSubscribe immediately at this time will cause the proportion of downloaded files queried by scheduledExecutorService to be less than 100%
     * According to the specific situation, delay a few seconds to call or actively send sendMessage(1,1)
     */
    fun  unSubscribe () {
        try {
            context.unregisterReceiver(receiver)
        }
        catch (e: java.lang.Exception){
        }
        finally {
            context.contentResolver.unregisterContentObserver(contentObserver)
            scheduledFuture?.cancel(true)
            downloadHandler.removeCallbacksAndMessages(null)
        }
    }

    interface DownloadListener {
        // Initialize the UI
        fun onPrepare() {}

        // Click the notification bar to call back
        fun onNotificationClicked() {}

        // Call back when the total download size is obtained, used to process the ProgressBar style separately
        fun onUnknownTotalSize() {}

        // Download progress callback
        fun onProgress(progress: Float)

        // Download complete
        fun onSuccess(path: String)

        // Download failed
        fun onFailed(throwable: Throwable)
    }

    companion object {
        private val TAG = UpToddDownloadManager::class.java.simpleName

        // Ensure that onUnknownTotalSize is called back once
        private var flag = false
        private var count = 0

        const val HANDLE_DOWNLOAD = 0x001

        private class DownloadHandler(val downloadListener: DownloadListener?) :
            Handler(Looper.getMainLooper()) {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == HANDLE_DOWNLOAD) {
//                    Log.d(TAG, "arg1=${msg.arg1},arg2=${msg.arg2}")
                    if (msg.arg1 >= 0 && msg.arg2 > 0) {
                        downloadListener?.onProgress(msg.arg1 / msg.arg2.toFloat())
                    } else {
                        count++
                        if (!flag && count > 5) {
                            downloadListener?.onUnknownTotalSize()
                            flag = true
                        }
                    }
                }
            }
        }
    }
}