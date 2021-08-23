package com.uptodd.uptoddapp.ui.todoScreens.todoDetailsScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

class TodoDetailsViewModel() : ViewModel() {

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String>
        get() = _imageUrl


    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description
    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    fun fetchTodoDetailsFromGetApi(todoId: Int) {
        viewModelScope.launch {
            _imageUrl.value = null
            val language = AllUtil.getLanguage()
            AndroidNetworking.get("https://www.uptodd.com/api/activities/{activityId}?lang=$language")
                .addPathParameter("activityId", "$todoId")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            val data = (response.get("data") as JSONObject)
                            _imageUrl.value = data.getString("image")
                            _description.value = data.getString("description")

                         Log.d("data",data.toString())
                            _title.value=data.getString("name")
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d(
                            "DetailsViewModel",
                            "API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )
                    }
                })
        }
    }
}