package com.uptodd.uptoddapp.ui.account.changepassword

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private const val TAG = "HSN_DEBUG"

class ChangePasswordViewModel : ViewModel() {
    private var currentPassword: String? = null
    var userId: String? = null
    var token: String? = null

    var isValidatingCurrentPassword = MutableLiveData<Boolean>()
    var isCurrentPasswordCorrect: Boolean = false

    var isUpdatingPassword = MutableLiveData<Boolean>()
    var isPasswordUpdated: Boolean = false
    var errorInChangePassword: String = "Something went wrong"


    fun checkCurrentPassword(enteredPassword: String) {
        if (currentPassword == null) {
            AndroidNetworking.get("https://uptodd.com/api/appusers/{userId}")
                .addPathParameter("userId", userId)
                .addHeaders("Authorization", "Bearer $token")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d(
                                "div",
                                "ChangePasswordViewModel L35 ${response.get("status")} -> ${
                                response.get(
                                    "data"
                                )
                                }"
                            )

                            var accountJSON =
                                (response.get("data") as JSONArray).get(0) as JSONObject
                            currentPassword = accountJSON.getString("password")
                            Log.d("div", "ChangePasswordViewModel L42 $currentPassword")
                            if (currentPassword == enteredPassword)
                                isCurrentPasswordCorrect = true
                            isValidatingCurrentPassword.value = false
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d(
                            "div",
                            "ChangePasswordViewModel L53 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )

                    }

                })
        } else {
            if (currentPassword == enteredPassword)
                isCurrentPasswordCorrect = true
            isValidatingCurrentPassword.value = false
        }
    }

    fun changePassword(newPassword: String, currentPassword: String, isDoctor: Boolean) {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("id", userId)
        jsonObject.put("password", newPassword)
        jsonObject.put("oldPassword", currentPassword)
        val date = LocalDate.now().toString()
        jsonObject.put("updateTime", date)
        Log.i(TAG, "sending $date")

        if (!isDoctor) {
            AndroidNetworking.put("https://uptodd.com/api/appusers/changepassword")
                .addHeaders("Authorization", "Bearer $token")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.d("div", "ChangePasswordViewModel L82 ${response?.get("status")}")
                        if (response != null && response.get("status") == "Success") {
                            Log.d("div", "ChangePasswordViewModel L84 ${response.get("status")}")

                            isPasswordUpdated = true
                            isUpdatingPassword.value = false
                        }
                    }

                    override fun onError(error: ANError?) {
                        isUpdatingPassword.value = false
                        Log.d("div", "AccountViewModel L67 $error")
                        if (error!!.getErrorCode() != 0) {
                            errorInChangePassword =
                                (error.errorBody as JSONObject).getString("message")
                            Log.d("div", "onError errorCode : " + error.getErrorCode())
                            Log.d("div", "onError errorBody : " + error.getErrorBody())
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        } else {
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                        }
                    }

                })
        } else {
            AndroidNetworking.put("https://uptodd.com/api/doctor/changepassword")
                .addHeaders("Authorization", "Bearer $token")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        Log.d("div", "ChangePasswordViewModel L82 ${response?.get("status")}")
                        if (response != null && response.get("status") == "Success") {
                            Log.d("div", "ChangePasswordViewModel L84 ${response.get("status")}")

                            isPasswordUpdated = true
                            isUpdatingPassword.value = false
                        }
                    }

                    override fun onError(error: ANError?) {
                        isUpdatingPassword.value = false
                        Log.d("div", "AccountViewModel L67 $error")
                        if (error!!.getErrorCode() != 0) {
                            errorInChangePassword =
                                (error.errorBody as JSONObject).getString("message")
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
}