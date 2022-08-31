package com.uptodd.uptoddapp.ui.remides.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uptodd.uptoddapp.ui.home.homePage.repo.HomPageRepository
import com.uptodd.uptoddapp.ui.remides.repo.RemediesRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RemediesViewModel(application: Application) : AndroidViewModel(application) {

    private val _remediesResponse = MutableLiveData<Pair<String, Any>>()
    val remediesResponse: LiveData<Pair<String, Any>>
        get() = _remediesResponse


    private val repository = RemediesRepository()


    fun getRemedies() {

        viewModelScope.launch {

            _remediesResponse.postValue(
                Pair(
                    HomPageRepository.Companion.AndroidNetworkingResponseWrapper.LOADING.name,
                    "Requesting Remedies.."
                )
            )

            repository.getRemediesResponse(success = {
                _remediesResponse.postValue(
                    Pair(
                        HomPageRepository.Companion.AndroidNetworkingResponseWrapper.SUCCESS.name,
                        it
                    )
                )
            }, error = { err, msg ->
                _remediesResponse.postValue(
                    Pair(
                        HomPageRepository.Companion.AndroidNetworkingResponseWrapper.ERROR.name,
                        err?.localizedMessage
                            ?: msg ?: "Unknown Error"
                    )
                )

            })

        }

    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}