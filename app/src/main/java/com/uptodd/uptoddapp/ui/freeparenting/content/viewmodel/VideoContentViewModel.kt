package com.uptodd.uptoddapp.ui.freeparenting.content.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressRequest
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.content.repo.VideoContentRepository
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

    private val videoRepository =
        VideoContentRepository(RetrofitSingleton.getInstance().getRetrofit())

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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }


}