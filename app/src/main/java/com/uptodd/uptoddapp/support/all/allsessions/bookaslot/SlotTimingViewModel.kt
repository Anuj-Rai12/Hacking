package com.uptodd.uptoddapp.support.all.allsessions.bookaslot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.util.*

class SlotTimingViewModel : ViewModel() {

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    fun resetState() {
        _isLoading.value = 201
    }


    fun bookASlot(
        slotDate: String,
        expertId: Int,
        expertName: String,
        slotTime: String,
        slotTimeTopic: String,
    ) {

        _isLoading.value = 1


        val jsonObject = JSONObject()
        jsonObject.put("userId", AllUtil.getUserId())
        jsonObject.put("sessionDateSelected", slotDate)
        jsonObject.put("sessionTopic", slotTimeTopic)
        if(expertName!="Any") {
            jsonObject.put("expertId", expertId)
            jsonObject.put("expertName", expertName)
        }
        jsonObject.put("bestTimeForSession", slotTime)

        AndroidNetworking.post("https://www.uptodd.com/api/appusers/sessions")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        if(response.getString("status").toLowerCase(Locale.ENGLISH) == "success"){
                            _isLoading.value = 0
                        }
                        else{
                            _isLoading.value = -1
                            apiError = response.getString("message")
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -1

                }
            })
    }
}