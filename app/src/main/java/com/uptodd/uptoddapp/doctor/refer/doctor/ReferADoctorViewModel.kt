package com.uptodd.uptoddapp.doctor.refer.doctor

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

class ReferADoctorViewModel : ViewModel() {

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""
    var variableError = ""

    var name = MutableLiveData<String>()
    var mail = MutableLiveData<String>()
    var phone = MutableLiveData<String>()
    var city = MutableLiveData<String>()

    init{
        _isLoading.value = 201
    }


    fun resetIsLoading(){
        _isLoading.value = 201
    }

    fun sendReferral() {
        if(name.value!!.trim().isEmpty()){
            variableError = "Name must not be empty!"
            _isLoading.value = -2
        }
        else if(mail.value!!.trim().isEmpty() || !AllUtil.isEmailValid(mail.value)){
            variableError = "Invalid e-mail address!"
            _isLoading.value = -2
        }
        else if(phone.value!!.trim().isEmpty() || phone.value!!.length!=10){
            variableError = "Invalid phone number!"
            _isLoading.value = -2
        }
        else if(city.value!!.trim().isEmpty()){
            variableError = "City must not be empty!"
            _isLoading.value = -2
        }
        else{
            _isLoading.value = 1
            val jsonObject = JSONObject()
            jsonObject.put("doctorId", AllUtil.getDoctorId())
            jsonObject.put("name", name.value)
            jsonObject.put("mail", mail.value)
            jsonObject.put("phone", phone.value)
            jsonObject.put("city", city.value)

            AndroidNetworking.post("https://uptodd.com/api/doctor/referreddoctors")
                .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        if (response.getString("status") == "Success") {
                            _isLoading.value = 0
                        } else {
                            apiError = response.getString("message")
                            _isLoading.value = -1
                        }
                    }

                    override fun onError(error: ANError) {
                        if(error.errorCode==0)
                            apiError = "Connection Timeout!"
                        else if(error.response!=null)
                            apiError = error.response.message()
                        else
                            apiError = error.message.toString()
                        _isLoading.value = -1
                        Log.i("error", "${error.errorBody} /n${error.response.message()} \n${error.errorDetail} \n${error.message} \n${error.localizedMessage} \n${error.cause}")
                    }
                })
        }
    }



}