package com.example.hackingwork.work

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.hackingwork.MainActivity
import com.example.hackingwork.R
import com.example.hackingwork.TAG
import com.example.hackingwork.utils.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UploadFileWorkManger constructor(
    context: Context,
    parameterName: WorkerParameters
) : CoroutineWorker(context, parameterName) {
    private var notificationManager: NotificationManager? = null
    private val channelId = "com.example.work"
    private var thumbnail: String? = null
    private var videoPreview: String? = null
    private var module: MutableMap<String, Module> = mutableMapOf()
    private var video: MutableMap<String, Video> = mutableMapOf()
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val storageReference: StorageReference by lazy {
        storage.getReferenceFromUrl("gs://hacking-e272c.appspot.com/")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return try {
            val output = inputData.getString(GetConstStringObj.EMAIL_VERIFICATION_LINK)
            Log.i(TAG, "doWork: $output")
            getValue(output)
            Log.i(TAG, "doWork Work: $module")
            val data = Data.Builder().putString(
                GetConstStringObj.EMAIL_VERIFICATION_LINK,
                Helper.serializeToJson(
                    (GetCourseContent(
                        thumbnail = thumbnail,
                        previewvideo = videoPreview,
                        module = module
                    ))
                )
            ).build()
            Log.i(
                TAG,
                "doWork Obj: ${
                    Helper.deserializeFromJson<GetCourseContent>(
                        data.getString(
                            GetConstStringObj.EMAIL_VERIFICATION_LINK
                        )
                    )
                }"
            )
            createNotification("All Task Complete!!", "CLICK ME to Processed Further", true)
            Result.Success(data)
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun getValue(output: String?) {
        output?.let { string ->
            val courseContent = Helper.deserializeFromJson<GetCourseContent>(string)
            courseContent?.let { getCourseContent ->
                getCourseContent.thumbnail?.let { setDataFile(it, "Thumbnail") }
                getCourseContent.previewvideo?.let { setDataFile(it, "PreviewVideo") }
                getCourseContent.module?.let { map ->
                    map.forEach { (module_key, value) ->
                        value.video?.forEach { (_, v) ->
                            uploadVideoFile(v, module_key, value)
                        }
                    }
                }
            }
            Log.i(TAG, "getValue: $module")
            return
        }
    }

    private suspend fun uploadVideoFile(
        video: Video,
        key: String,//Module Key
        value: Module
    ) {
        uploadingVideo(video, key).collect {
            when (it) {
                is MySealed.Error -> {
                    createNotification(
                        "${video.title}",
                        "${it.exception?.localizedMessage}"
                    )
                    Log.i(
                        TAG,
                        "uploadVideoFile  Error: ${value.module} of ${video.title} is -> ${it.exception?.localizedMessage}"
                    )
                }
                is MySealed.Loading -> createNotification("${value.module}", it.data as String)
                is MySealed.Success -> {
                    val downloadUri = it.data as MutableList<*>
                    val videoUri = downloadUri.first() as Uri
                    val assignmentUri = if (videoUri != downloadUri.last() as Uri) {
                        (downloadUri.last() as Uri).toString()
                    } else
                        null
                    val vid = Video(
                        title = video.title, uri = videoUri.toString(), duration = video.duration,
                        assignment = Assignment(
                            title = video.assignment?.title,
                            uri = assignmentUri
                        )
                    )
                    if (!this.module.containsKey(value.module)) {
                        val map = mutableMapOf<String, Video>()
                        this.video = map
                    }
                    this.video[vid.title!!] = vid
                    val module = Module(module = value.module, video = this.video)
                    this.module[value.module!!] = module
                    Log.i(TAG, "uploadVideoFile: ${this.module}")
                }
            }
        }
    }

    private fun uploadingVideo(video: Video, key: String /*Module Key */) = flow {
        emit(MySealed.Loading("${video.title} is Uploading..."))
        val data = try {
            val downloadUri = mutableListOf<Uri>()
            val videoRef = storageReference.child("$key/${video.title}")
            videoRef.putFile(video.uri?.toUri()!!).await()
            downloadUri.add(videoRef.downloadUrl.await())
            video.assignment?.uri?.let {
                val assignmentRef =
                    storageReference.child("$key/${video.title}/${video.assignment.title}")
                assignmentRef.putFile(it.toUri()).await()
                downloadUri.add(assignmentRef.downloadUrl.await())
            }
            MySealed.Success(downloadUri)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }

    private suspend fun setDataFile(thumbnail: String, string: String) {
        uploading(thumbnail).collect {
            when (it) {
                is MySealed.Error -> createNotification(string, "${it.exception?.localizedMessage}")
                is MySealed.Loading -> createNotification("$string...", "Uploading $string...")
                is MySealed.Success -> {
                    val uri = (it.data as Uri).toString()
                    when (string) {
                        "Thumbnail" -> this.thumbnail = uri
                        "PreviewVideo" -> this.videoPreview = uri
                    }
                }
            }
        }
    }

    private fun uploading(string: String) = flow {
        emit(MySealed.Loading("Uploading Course..."))
        val data = try {
            val str =
                storageReference.child("ThumbnailAndVideoPreVideo/${string.toUri().lastPathSegment}")
            str.putFile(string.toUri()).await()
            val download = str.downloadUrl.await()
            MySealed.Success(download)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }

    private fun createNotification(
        name: String,
        desc: String,
        flag: Boolean = false
    ) {
        val notificationId = 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = desc
                    enableLights(true)
                    lightColor = Color.GREEN
                }
            notificationManager?.createNotificationChannel(channel)
        }
        val intent = Intent(applicationContext, MainActivity::class.java)
        if (flag) {
            intent.putExtra(
                GetConstStringObj.VERSION,
                Helper.serializeToJson(
                    GetCourseContent(
                        thumbnail = thumbnail,
                        previewvideo = videoPreview,
                        module = module
                    )
                )
            )
            Log.i(TAG, "createNotification: Data Is Set for Task Complete")
        }
        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(101, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(name)
            .setContentText(desc)
            //.setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_upload)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
        //.build()
        if (flag)
            notification.setContentIntent(pendingIntent)
        notificationManager?.notify(notificationId, notification.build())
    }
}