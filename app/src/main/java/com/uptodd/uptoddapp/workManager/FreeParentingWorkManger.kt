package com.uptodd.uptoddapp.workManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.setLogCat
import kotlinx.coroutines.flow.collectLatest

class FreeParentingWorkManger(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {


    private val videoRepository =
        VideoContentRepository(
            RetrofitSingleton.getInstance().getRetrofit(),
            UptoddDatabase.getInstance(context).videoContentDao
        )


    override suspend fun doWork(): Result {
        return try {
            getDeleteDb()
            getVideoContent()
            Result.success()
        } catch (e: Exception) {
            setLogCat("WORK_FREE", e.localizedMessage?:"Work Error")
            Result.failure()
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
                        val item = getVideoContentFromList(videoContentList)
                        setLogCat("DB_Response", " item list is => $item")
                    }
                }
            }
        }
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
                    setLogCat("DB_Response", "${it.data}")
                    if (it.data !is String) {

                        val item = it.data as List<Content>
                        if (item.isEmpty()) {
                          //  videoContentVideoModel.getVideoContent()
                        }
                    }
                }
            }
        }
    }


    private fun getVideoContentFromList(videoContentList: VideoContentList): MutableList<Content> {
        val mutableList = mutableListOf<Content>()
        videoContentList.data.forEach { data ->
            val item = data.content.filter { content ->
                content.type == FreeDemoVideoModuleFragments.Companion.VideoContentTabsEnm.MUSIC.name
            }
            mutableList.addAll(item)
        }
        return mutableList
    }


}