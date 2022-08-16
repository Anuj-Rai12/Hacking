package com.uptodd.uptoddapp.ui.freeparenting.daily_book.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DailyBookVideoModel(application: Application) : AndroidViewModel(application) {

    private val app = application

    private var repository: VideoContentRepository =
        VideoContentRepository(
            RetrofitSingleton.getInstance().getRetrofit(),
            UptoddDatabase.getInstance(application).videoContentDao
        )

    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event

    private val _videoContentResponseFromApi = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val videoContentResponseFromApi: LiveData<ApiResponseWrapper<out Any?>>
        get() = _videoContentResponseFromApi

    private val _getVideoContentFromDb = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val getVideoContentFromDb: LiveData<ApiResponseWrapper<out Any?>>
        get() = _getVideoContentFromDb

    private val _tabListAndContent = MutableLiveData<List<Pair<String, List<Content>>>>()
    val tabListAndContent: LiveData<List<Pair<String, List<Content>>>>
        get() = _tabListAndContent


    fun getVideoContentApi() {
        if (!app.isNetworkAvailable()) {
            _event.postValue(Event("No Internet Connection Found"))
            return
        }
        viewModelScope.launch {
            repository.getVideoContent().collectLatest {
                _videoContentResponseFromApi.postValue(it)
            }
        }
    }


    fun getVideoContentDb() {
        viewModelScope.launch {
            repository.getAllVideoFromDb().collectLatest {
                _getVideoContentFromDb.postValue(it)
            }
        }
    }


    fun displayData(videoContentList: VideoContentList) {
        if (videoContentList.data.isEmpty()) {
            _event.postValue(Event("No VideoContent is Present Currently.."))
            return
        }
        viewModelScope.launch {
            val def = async(IO) {
                val mutableListOfTab = mutableListOf<Pair<String, List<Content>>>()
                videoContentList.data.forEach {
                    mutableListOfTab.add(Pair(it.section, it.content))
                }
                mutableListOfTab
            }
            _tabListAndContent.postValue(def.await())
        }

    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}