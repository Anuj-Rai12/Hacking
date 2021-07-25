package com.uptodd.uptoddapp.ui.login.forgetpassword.forgetpassword

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class ForgetPasswordViewModel :ViewModel()
{
    var otp:String?=null
    var userId:String?=null
    fun checkEmailIsValid(email:String):Boolean
    {
        if(!email.contains('@') || !email.contains('.'))
            return false
        else if(email.length<8)
            return false
        else return !(email.indexOf('@')-0<3 || email.lastIndexOf('.')-email.indexOf('@')<4 || email.length-email.lastIndexOf('.')<4)
    }

    var isLoadingDialogVisible=MutableLiveData<Boolean>()
    var isEmailSent=false
    fun sendOTP(email: String, isDoctorReset: Boolean) {

        val jsonObject=JSONObject()
        jsonObject.put("email",email)

        if(!isDoctorReset) {
            AndroidNetworking.post("https://www.uptodd.com/api/appusers/forgotpassword")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.d("div", "ForgetPasswordViewModel L46 $response")
                        if (response != null && response.get("status") == "Success") {
                            Log.d("div", "ForgetPasswordViewModel L52 ${response.get("data")}")
                            otp = response.get("data").toString()
                            //otp=(response.get("data") as JSONObject).getString("otp").toString()
                            //userId=(response.get("data") as JSONObject).getString("userId").toString()
                            isEmailSent = true
                            isLoadingDialogVisible.value = false
                        }
                    }

                    override fun onError(error: ANError?) {
                        isLoadingDialogVisible.value = false
                        Log.d("div", "ForgetPasswordViewModel L67 $error")
                        if (error!!.getErrorCode() != 0) {
                            Log.d("div", "onError errorCode : " + error.getErrorCode())
                            Log.d("div", "onError errorBody : " + error.getErrorBody())
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        }
                    }

                })
        }
        else{
            AndroidNetworking.post("https://www.uptodd.com/api/doctor/forgotpassword")
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.d("div", "ForgetPasswordViewModel L46 $response")
                        if (response != null && response.get("status") == "Success") {
                            Log.d("div", "ForgetPasswordViewModel L52 ${response.get("data")}")
                            otp = response.get("data").toString()
                            Log.i("otp", otp.toString())
                            //otp=(response.get("data") as JSONObject).getString("otp").toString()
                            //userId=(response.get("data") as JSONObject).getString("userId").toString()
                            isEmailSent = true
                            isLoadingDialogVisible.value = false
                        }
                    }

                    override fun onError(error: ANError?) {
                        isLoadingDialogVisible.value = false
                        Log.d("div", "ForgetPasswordViewModel L67 $error")
                        if (error!!.getErrorCode() != 0) {
                            Log.d("div", "onError errorCode : " + error.getErrorCode())
                            Log.d("div", "onError errorBody : " + error.getErrorBody())
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        }
                    }

                })
        }
    }

    fun checkOTP(enteredOtp: String):Boolean {
        return otp==enteredOtp
    }


}