package com.uptodd.uptoddapp.doctor.bookasession

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class BookASessionViewModel : ViewModel() {

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    lateinit var sharedPreferences: SharedPreferences

    var apiError = ""
    var variableError = ""

    var name = MutableLiveData<String>()
    var mail = MutableLiveData<String>()
    var whatsapp = MutableLiveData<String>()
    var babyAge = MutableLiveData<String>()

    init {
        _isLoading.value = 201
        name.value = ""
        mail.value = ""
        whatsapp.value = ""
        babyAge.value = ""
    }

    fun resetLoading(){
        _isLoading.value = 201
    }

    fun bookSession(){
        _isLoading.value = 1
        val errorCode = variableCheck()
        if(errorCode == -1) {
            val jsonObject = JSONObject()
            jsonObject.put("referredById", AllUtil.getDoctorId())
            jsonObject.put("referredBy", "doctor")
            jsonObject.put("patientName", name.value)
            jsonObject.put("babyAge", babyAge.value)
            jsonObject.put("patientWhatsapp", whatsapp.value)
            if(mail.value!!.isNotEmpty() && AllUtil.isEmailValid(mail.value))
                jsonObject.put("patientMail", mail.value)



            //TODO: remove these comments when api is compatible
/*        if(babyName.isNotEmpty())
//            jsonObject.put("babyName", newTicket.subject)
//        if(babyDOB.isNotEmpty())
//            jsonObject.put("babyDOB", babyDOB)
//        if(feedback.isNotEmpty())
//            jsonObject.put("message", feedback)*/

            AndroidNetworking.post("https://www.uptodd.com/api/referredpatients")
                .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {

                            if(sharedPreferences.getBoolean("firstReferral", true))
                                sharedPreferences.edit().putBoolean("firstReferral", false).apply()

                            sharedPreferences.edit().putLong("latestReferral", System.currentTimeMillis()).apply()

                            _isLoading.value = 0
                        } else {
                            apiError = response.getString("message")
                            _isLoading.value = -1
                        }
                    }

                    override fun onError(error: ANError) {
                        apiError = error.message.toString()
                        _isLoading.value = -1
                        Log.i("error", error.errorBody)
                    }
                })
        }
        else{
            variableError = (when(errorCode){
                0 -> "Name must not be empty!"
                1 -> "Whatsapp No is required!"
                else -> "Please enter baby age"
            })
            _isLoading.value = -2
        }
    }

    private fun variableCheck(): Int {
        if(name.value!!.isEmpty())
            return 0
        if(whatsapp.value!!.isEmpty())
            return 1
        if(babyAge.value!!.isEmpty())
            return 2
        return -1
    }

}