package com.uptodd.uptoddapp.ui.refer.refer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject

class ReferViewModel :ViewModel()
{
    var token: String?=null
    var uid: String=""
    var isReferralSentSuccess=MutableLiveData(201)
    var code=MutableLiveData<String>("fcs2ds8")     //TODO
    val appLink="com.uptodd.uptoddapp"

    var apiError = ""

    fun sendReferral(name: String, email: String, phone: String, timestamp: Long) {
        isReferralSentSuccess.value = 0
        val jsonObject = JSONObject()
        jsonObject.put("referredById", uid)
        jsonObject.put("referredBy", "patient")
        jsonObject.put("patientName", name)
        jsonObject.put("patientMail", email)
        jsonObject.put("patientPhone", phone)

        AndroidNetworking.post("https://www.uptodd.com/api/referredpatients")
            .addHeaders("Authorization","Bearer $token")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        isReferralSentSuccess.value=1
                    } else {
                        apiError = response.getString("message")
                        isReferralSentSuccess.value=-1
                    }
                }

                override fun onError(error: ANError) {
                    apiError = error.message.toString()
                    isReferralSentSuccess.value=-1
                }
            })
    }

}