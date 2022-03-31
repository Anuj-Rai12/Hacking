package com.uptodd.uptoddapp.ui.monthlyDevelopment

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.all.All
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.DevelopmentTracker
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception

class DevelopmentTrackerViewModel(applicationContext:Application):AndroidViewModel(applicationContext) {

    var trackerList : MutableLiveData<DevelopmentTracker> = MutableLiveData()
    var errorResponse : MutableLiveData<Boolean> = MutableLiveData()


    fun fetchTrackerResponse(context: Context){

        val userId = AllUtil.getUserId()

        AndroidNetworking.get("https://uptodd.com/api/appusers/previousTrackerResponses/$userId")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        Log.d("Response",response.toString() )
                        val devResponse = AllUtil.getDevelopmentTrackerResponse(response.toString())
                        trackerList.postValue(devResponse)
                    } catch (e:Exception) {
                        errorResponse.postValue(true)
                    }
                }

                override fun onError(error: ANError) {
                    errorResponse.postValue(true)
                    Log.i("error", error.errorBody)
                }
            })


    }



}