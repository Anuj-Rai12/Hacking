package com.uptodd.uptoddapp.doctor

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.logindetails.DoctorLoginInfo
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class DoctorLoginViewModel : ViewModel() {

    var emailId = MutableLiveData<String>()
    var password = MutableLiveData<String>()

    var incorrectEmail = MutableLiveData<Boolean>()
        private set
    var incorrectPassword = MutableLiveData<Boolean>()
        private set

    var emailMsg = ""
    var passwordMsg = ""

    var apiError = MutableLiveData<String>()
        private set

    var showLoginProgress = MutableLiveData<Boolean>(false)
    var enableInput = MutableLiveData<Boolean>(true)

    var loginResponse = MutableLiveData<DoctorLoginInfo>()

    fun beginLogin() {

        toggleUi()

        val emailEmpty = checkEmailEmpty()
        val passwordEmpty = checkPasswordEmpty()

        if (emailEmpty || passwordEmpty) {
            emailMsg = "Email cannot be empty"
            passwordMsg = "Password cannot be empty"
            incorrectEmail.value = emailEmpty
            incorrectPassword.value = passwordEmpty

            toggleUi()
            return
        }

        login()
    }

    private fun toggleUi() {
        showLoginProgress.value = !showLoginProgress.value!!
        enableInput.value = !enableInput.value!!
    }

    fun checkEmailEmpty() = emailId.value.isNullOrEmpty()
    fun checkPasswordEmpty() = password.value.isNullOrEmpty()

    private fun login() {
        val emailId = requireNotNull(emailId.value)
        val password = requireNotNull(password.value)

        val loginCreds = JSONObject().apply {
            put("mail", emailId)
            put("password", password)
        }

        AndroidNetworking.post("https://uptodd.com/api/doctor")
            .addJSONObjectBody(loginCreds)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("response", response.toString())
                        if (response.getString("status") == "Success") {
                            val uid =
                                (response.getJSONObject("data") as JSONObject).getJSONObject("doctor")
                                    .getString("id")
                            val email =
                                (response.getJSONObject("data") as JSONObject).getJSONObject("doctor")
                                    .getString("mail")
                            val token = response.getJSONObject("data").getString("token")

                            val userType = "Doctor"

                            val doctorInfo = DoctorLoginInfo(uid, email, userType, token, true)
                            loginResponse.value = doctorInfo
                            AllUtil.registerToken("doctor")
                            toggleUi()
                        }
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0) {
                        apiError.value = "Connection Timeout!"
                    } else {
                        apiError.value =
                            AllUtil.getJsonObject(anError.errorBody).getString("message")
                                .toString()
                        if (apiError.value == "Incorrect Credentials") {
                            passwordMsg = "Incorrect Password"
                            emailMsg = "Incorrect Email"
                            incorrectEmail.value = true
                            incorrectPassword.value = true
                        }
                    }

                    AllUtil.logApiError(anError)
                    toggleUi()
                }
            })

    }

}