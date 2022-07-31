package com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressRequest
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
import com.uptodd.uptoddapp.ui.freeparenting.content.tabs.FreeDemoVideoModuleFragments
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VideoContentViewModel(application: Application) : AndroidViewModel(application) {

    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event

    private val _videoContentResponse = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val videoContentResponse: LiveData<ApiResponseWrapper<out Any?>>
        get() = _videoContentResponse


    private val _updateUserProgressResponse = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val updateUserProgressResponse: LiveData<ApiResponseWrapper<out Any?>>
        get() = _updateUserProgressResponse


    private val _getVideoContentResponse = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val getVideoContentResponse: LiveData<ApiResponseWrapper<out Any?>>
        get() = _getVideoContentResponse


    private val videoRepository =
        VideoContentRepository(
            RetrofitSingleton.getInstance().getRetrofit(),
            UptoddDatabase.getInstance(application).videoContentDao
        )

    private val app = application

    fun getVideoContent() {
        if (app.isNetworkAvailable()) {
            viewModelScope.launch {
                videoRepository.getVideoContent().collectLatest {
                    _videoContentResponse.postValue(it)
                }
            }
        } else {
            _event.postValue(Event("No Internet Connection found!!"))
        }
    }


    fun updateUserProgress(request: UpdateUserProgressRequest) {
        if (app.isNetworkAvailable()) {
            viewModelScope.launch {
                videoRepository.updateUserProgress(request).collectLatest {
                    _updateUserProgressResponse.postValue(it)
                }
            }
        } else {
            _event.postValue(Event("No Internet Connection found!!"))
        }
    }


    fun getVideoContentItem() {
        viewModelScope.launch {
            videoRepository.getAllVideoFromDb().collectLatest {
                _getVideoContentResponse.postValue(it)
            }
        }
    }


    fun insertAllItemInDb(content: List<Content>) {
        viewModelScope.launch {
            videoRepository.getInsetVideoFromDb(content).collectLatest {
                _getVideoContentResponse.postValue(it)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


    fun deleteVideoItemInDb() {
        viewModelScope.launch {
            videoRepository.deleteVideoFromDb().collectLatest {
                _getVideoContentResponse.postValue(it)
            }
        }
    }


    fun getVideoContentFromList(videoContentList: VideoContentList): MutableList<Content> {
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