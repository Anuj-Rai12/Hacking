package com.uptodd.uptoddapp.ui.freeparenting.purchase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeRequest
import com.uptodd.uptoddapp.module.RetrofitSingleton
import com.uptodd.uptoddapp.ui.freeparenting.purchase.repo.CourseUpgradeRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.Event
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.isNetworkAvailable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val courseUpgradeRepository =
        CourseUpgradeRepository(RetrofitSingleton.getInstance().getRetrofit())

    private val _event = MutableLiveData<Event<String>>()
    val event: LiveData<Event<String>>
        get() = _event


    private val _upgradeCourseResponse = MutableLiveData<ApiResponseWrapper<out Any>>()
    val upgradeCourseResponse: LiveData<ApiResponseWrapper<out Any>>
        get() = _upgradeCourseResponse

    private val app = application
    fun doCourseUpgrade(id: Long) {
        if (!app.isNetworkAvailable()) {
            _event.postValue(Event(FilesUtils.NO_INTERNET))
            return
        }
        viewModelScope.launch {
            courseUpgradeRepository.postUpGradeRequest(UpgradeRequest(id)).collectLatest {
                _upgradeCourseResponse.postValue(it)
            }
        }
    }

}