package com.uptodd.uptoddapp.ui.freeparenting.dashboard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.datamodel.feedback.FeedBackRequest
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.dashboard.repo.FeedBackRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FeedBackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FeedBackRepository(RetrofitSingleton.getInstance().getRetrofit())

    private val _sendFeedBackResponse = MutableLiveData<ApiResponseWrapper<out String>>()
    val sendFeedBackResponse: LiveData<ApiResponseWrapper<out String>>
        get() = _sendFeedBackResponse


    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event


    private val app = application

    fun sendResponse(request: FeedBackRequest) {
        if (!app.isNetworkAvailable()) {
            _event.postValue(Event(FilesUtils.NO_INTERNET))
            return
        }
        viewModelScope.launch {
            repository.sendFeedBack(request).collectLatest {
                _sendFeedBackResponse.postValue(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}