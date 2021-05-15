package com.uptodd.uptoddapp.ui.refer.referdetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ReferDetailsViewModel :ViewModel()
{
    var token: String?=null
    var uid: String=""
    private var apiError: String = ""
    var referralName = "55"
    var referralEmail = MutableLiveData<String>("")
    var referralPhone = MutableLiveData<String>("9871237564")
    var referralDate = ""
    var referralStatus = ""
    var referralRegistrationDate = "Not registered"

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    init {
        _isLoading.value = 201
    }

    fun submit() {
        val jsonObject = JSONObject()
        jsonObject.put("id", uid.toInt())
        jsonObject.put("patientMail", referralEmail.value)
        jsonObject.put("patientPhone", referralPhone.value)
        jsonObject.put("feedback","uptodd")
        _isLoading.value = 1

        Log.d("div","ReferDetailsViewModel L47 ${referralEmail.value} ${referralPhone.value}")

        AndroidNetworking.put("https://uptodd.com/api/referredpatients")
            .addHeaders("Authorization","Bearer $token")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div","ReferDetailsViewModel L56 $response")
                    if (response != null) {
                        Log.d("div","ReferDetailsViewModel L56 $response")
                        if(response.getString("status")=="Success")
                            _isLoading.value = 0
                        else{
                            apiError = response.getString("message")
                            _isLoading.value = -1
                            Log.e("apiError", response.toString())
                        }
                        Log.i("response", response.toString())
                    }
                }

                override fun onError(error: ANError?) {
                    Log.d("div", "AccountViewModel L67 $error")
                    if (error!!.getErrorCode() != 0) {
                        Log.d("div", "onError errorCode : " + error.getErrorCode())
                        Log.d("div", "onError errorBody : " + error.getErrorBody())
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    }
                    if (error.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = error.message.toString()
                    _isLoading.value = -1
                }
            })
    }

    fun setUp(referralPerson: ReferredListItemPatient) {
        referralName = referralPerson.patientName!!
        referralEmail.value = referralPerson.patientMail!!
        referralDate = getDateFromTime(referralPerson.registrationDateValue)
        referralStatus = referralPerson.referralStatus!!
        referralPhone.value =referralPerson.patientPhone!!
        if(referralStatus=="Success")
            referralRegistrationDate=getDateFromTime(referralPerson.registrationDateValue)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateFromTime(time: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(time))
    }

}