package com.uptodd.uptoddapp.ui.freeparenting.login.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.login.repo.LoginRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _loginResponse = MutableLiveData<ApiResponseWrapper<out Any?>>()
    val loginResponse: LiveData<ApiResponseWrapper<out Any?>>
        get() = _loginResponse

    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event

    private val loginRepository =
        LoginRepository(RetrofitSingleton.getInstance().getRetrofit(), application)
    private val context = application

    fun fetchResponse(request: FreeParentingLoginRequest) {
        if (context.isNetworkAvailable()) {
            viewModelScope.launch {
                loginRepository.getSignInUserInfo(request).collectLatest {
                    _loginResponse.postValue(it)
                }
            }
        } else {
            _event.postValue(Event(FilesUtils.NO_INTERNET))
        }
    }

    val getRequestLoginRequest: FreeParentingLoginRequest?
        get() = loginRepository.getLoginPreferences()


    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
}