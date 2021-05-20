package com.uptodd.uptoddapp.media.resources

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.media.music.MusicFiles
import com.uptodd.uptoddapp.database.media.resource.ResourceFiles
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

class ResourceViewModel :ViewModel()
{
    private var _resources= MutableLiveData<ArrayList<ResourceFiles>>()
    val resources: LiveData<ArrayList<ResourceFiles>>
        get() = _resources

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    fun getAllResources() {
        AndroidNetworking.get("https://uptodd.com/api/resources")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        viewModelScope.launch {
                            val  resources = AllUtil.getAllResources(response.get("data").toString())
                            _resources.value=resources
                            _isLoading.value = 0
                        }

                    } else {
                        _isLoading.value = -1
                    }
                }

                override fun onError(error: ANError) {
                    _isLoading.value = -1
                    Log.i("error", error.errorBody)
                }
            })
    }






}