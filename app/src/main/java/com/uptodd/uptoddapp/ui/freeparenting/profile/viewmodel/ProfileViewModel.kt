package com.uptodd.uptoddapp.ui.freeparenting.profile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.profile.repo.ProfileRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var profileRepository: ProfileRepository =
        ProfileRepository(RetrofitSingleton.getInstance().getRetrofit())

    private val app = application

    private val _profileResponse = MutableLiveData<ApiResponseWrapper<out Any>>()
    val profileResponse: LiveData<ApiResponseWrapper<out Any>>
        get() = _profileResponse

    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event

    fun getProfile(id: Long) {
        if (!app.isNetworkAvailable()) {
            _event.postValue(Event(FilesUtils.NO_INTERNET))
            return
        }
        viewModelScope.launch {
            profileRepository.getProfile(id).collectLatest {
                _profileResponse.postValue(it)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}