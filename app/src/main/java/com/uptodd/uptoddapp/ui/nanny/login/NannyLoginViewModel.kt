package com.uptodd.uptoddapp.ui.nanny.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NannyLoginViewModel : ViewModel() {

    var isNewUser: Boolean = true
    var uid: String = ""
    var email: String = ""
    var loginMethod: String = ""
    var kidsDob: String = ""           //yyyy-mm-dd and "null" if pre-birth. "null" not null
    var babyName: String = ""
    var babyDOB: Long = 0L
    var profileImageUrl: String = ""
    var tokenHeader: String = ""

    var isValidatingEmailPassword = MutableLiveData<Boolean>()
    var errorFromApiResponse = MutableLiveData<String>()

    var enableInput = MutableLiveData<Boolean>(true)

    var loginId = MutableLiveData<String>()
    var sPassword = MutableLiveData<String>()

    var incorrectLoginId = MutableLiveData<Boolean>()
        private set
    var incorrectPassword = MutableLiveData<Boolean>()
        private set

    val loginResponse = MutableLiveData<UserInfo>()

    var loginIdMsg = ""
    var passwordMsg = ""
    var showLoginProgress = MutableLiveData<Boolean>()

    fun beginLogin() {
        loginStarted()
        val isEmailEmpty = checkEmail()
        val isPasswordEmpty = checkPassword()

        if (isEmailEmpty || isPasswordEmpty) {
            loginIdMsg = "Email cannot be empty"
            passwordMsg = "Password cannot be empty"
            incorrectLoginId.value = isEmailEmpty
            incorrectPassword.value = isPasswordEmpty
            loginEnd()
            return
        }

        login()

    }

    fun loginStarted() {
        showLoginProgress.value = true
        enableInput.value = false
    }

    fun loginEnd() {
        showLoginProgress.value = false
        enableInput.value = true
    }

    private fun login() {
        val loginId = requireNotNull(loginId.value)
        val password = requireNotNull(sPassword.value)
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("nannyLoginId", loginId)
        jsonObject.put("nannyLoginPassword", password)

        AndroidNetworking.post("https://uptodd.com/api/appusers/nanny")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null && response.get("status") == "Success") {
                        loginMethod = "EmailPassword"
                        email =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString("email")
                        if (((response.get("data") as JSONObject).getJSONObject("user")).getString("motherStage") != "null")
                            isNewUser = false
                        uid =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getLong("id")
                                .toString()
                        kidsDob =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString("kidsDob")
                        babyName =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString("kidsName")
                        if (babyName == "null")
                            babyName = "baby"
                        profileImageUrl =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString("profileImageUrl")
                        babyDOB =
                            SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH).parse(kidsDob)!!.time
                        tokenHeader = (response.get("data") as JSONObject).getString("token")

                        val userInfo = UserInfo(
                            uid,
                            isNewUser,
                            "Nanny",
                            email,
                            loginMethod,
                            kidsDob,
                            babyName,
                            babyDOB,
                            profileImageUrl,
                            tokenHeader,
                            "",
                            false,
                            System.currentTimeMillis(),
                            tokenHeader,
                            true
                        )
                        loginResponse.value = userInfo
                        loginEnd()
                    }
                }

                override fun onError(error: ANError?) {

                    if (error!!.getErrorCode() != 0) {
                        val errorRes = error.errorBody.substring(29, error.errorBody.length - 14)

                        if (errorRes == "Incorrect Credentials") {
                            loginIdMsg = "Incorrect LoginId"
                            passwordMsg = "Incorrect Password"
                            incorrectLoginId.value = true
                            incorrectPassword.value = true
                        }
                        errorFromApiResponse.value = errorRes
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    }

                    loginEnd()
                }

            })
    }


    fun checkEmail() = loginId.value.isNullOrBlank()

    fun checkPassword() = sPassword.value.isNullOrBlank()

}