package com.uptodd.uptoddapp.ui.login.forgetpassword.resetpassword

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import java.time.LocalDate

class ResetPasswordViewModel : ViewModel() {

    var passwordUpdateComplete = MutableLiveData<Boolean>()
    var showErrorDailog = MutableLiveData<String>()


    var email: String = ""
    var isDoctorReset = false

    var newPassword = MutableLiveData<String>()
    var confirmPassword = MutableLiveData<String>()

    var passwordMisMatch = MutableLiveData<Boolean>()

    var newPasswordError = MutableLiveData<Boolean>()
        private set
    var confirmPasswordError = MutableLiveData<Boolean>()
        private set

    var newPasswordMsg = ""
    var confirmPasswordMsg = ""

    var showUpdateProgress = MutableLiveData<Boolean>()

    var enableInput = MutableLiveData<Boolean>(true)

    fun setArguments(emailId: String, doctorReset: Boolean) {
        email = emailId
        isDoctorReset = doctorReset
    }

    fun beginPasswordReset() {

        passwordResetStarted()

        val newPasswordEmpty = newPassword.value.isNullOrEmpty()
        val confirmpasswordEmpty = confirmPassword.value.isNullOrEmpty()

        if (newPasswordEmpty || confirmpasswordEmpty) {
            newPasswordMsg = "password cannot be empty"
            confirmPasswordMsg = "password cannot be empty"
            newPasswordError.value = true
            confirmPasswordError.value = true
            passwordResetEnded()
            return
        }

        val newPassword = requireNotNull(newPassword.value)
        val conPassword = requireNotNull(confirmPassword.value)

        if (newPassword != conPassword) {
            passwordMisMatch.value = true
            passwordResetEnded()
            return
        }

        changePassword()

    }

    fun passwordResetStarted() {
        showUpdateProgress.value = true
        enableInput.value = false
    }

    fun passwordResetEnded() {
        showUpdateProgress.value = false
        enableInput.value = true
    }

    fun changePassword() {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", newPassword.value)
        val date = LocalDate.now().toString()
        jsonObject.put("updateTime", date)
        if (!isDoctorReset) {
            AndroidNetworking.post("https://uptodd.com/api/appusers/newpassword")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null && response.get("status") == "Success") {
                            passwordResetEnded()
                            passwordUpdateComplete.value = true
                        }
                    }

                    override fun onError(error: ANError?) {
                        if (error == null) return
                        val msg = error.errorBody.split(",").get(1).split(":").last()
                        showErrorDailog.value = msg
                        passwordResetEnded()
                    }
                })
        } else {
            AndroidNetworking.post("https://uptodd.com/api/doctor/newpassword")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.d("div", "ChangePasswordViewModel L82 ${response?.get("status")}")
                        if (response != null && response.get("status") == "Success") {
                            passwordResetEnded()
                            passwordUpdateComplete.value = true
                        }
                    }

                    override fun onError(error: ANError?) {
                        Log.d("div", "AccountViewModel L67 $error")
                        showErrorDailog.value = error?.errorBody
//                        if (error!!.getErrorCode() != 0) {
//
//                        } else {
//                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
//                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
//                        }
                        passwordResetEnded()
                    }

                })
        }
    }

}