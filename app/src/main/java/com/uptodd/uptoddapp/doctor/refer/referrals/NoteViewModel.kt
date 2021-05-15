package com.uptodd.uptoddapp.doctor.refer.referrals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class NoteViewModel : ViewModel() {

    private var _isLoading = MutableLiveData<Int>()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    init {
        _isLoading.value = 201
    }



    fun submitNote(isDoctor: Boolean, referredPersonId: Int, note: String, ){
        if(isDoctor) {
            val jsonObject = JSONObject()
            jsonObject.put("id", referredPersonId)
            jsonObject.put("doctorFeedback", note)
            _isLoading.value = 1

            AndroidNetworking.put("https://uptodd.com/api/doctor/referreddoctors")
                .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            if(response.getString("status")=="Success")
                                _isLoading.value = 0
                            else{
                                apiError = response.getString("message")
                                _isLoading.value = -1
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        if (anError!!.errorCode == 0)
                            apiError = "Connection Timeout!"
                        else
                            apiError = anError.message.toString()
                        AllUtil.logApiError(anError)
                        _isLoading.value = -1
                    }
                })
        }
        else{
            val jsonObject = JSONObject()
            jsonObject.put("id", referredPersonId)
            jsonObject.put("feedback", note)
            _isLoading.value = 1

            AndroidNetworking.put("https://uptodd.com/api/referredpatients")
                .addJSONObjectBody(jsonObject)
                .addHeaders("Content-Type", "application/json")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            if(response.getString("status")=="Success")
                                _isLoading.value = 0
                            else{
                                apiError = response.getString("message")
                                _isLoading.value = -1
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

}