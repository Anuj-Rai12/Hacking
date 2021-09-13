package com.uptodd.uptoddapp.ui.nanny.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NannyLoginViewModel : ViewModel() {

    var isNewUser: Boolean = true
    var userName:String="No Name"
    var uid: String = ""
    var email: String = ""
    var loginMethod: String = ""
    var kidsDob: String = ""  //yyyy-mm-dd and "null" if pre-birth. "null" not null
    var babyName: String = ""
    var babyDOB: Long = 0L
    var profileImageUrl: String = ""
    var tokenHeader: String = ""
    var parentType: String = ""
    var motherStage=""
    var kidsGender=""
    var subscriptionStartDate=""
    var subsriptionEndDate=""
    var phoneNo=""
    var address=""
    var appAccessingDate=""
    var currentPlan:Long=0

    var isValidatingEmailPassword = MutableLiveData<Boolean>()
    var errorFromApiResponse = MutableLiveData<String>()

    var enableInput = MutableLiveData<Boolean>(true)
    var iSNPNew=MutableLiveData<Boolean>()

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
        var token:String=" "
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                token = if (!task.isSuccessful) {
                    " "
                } else
                    task.result.token
            }
        val loginCreds = JSONObject().apply {
                    put("nannyLoginId", loginId)
                    put("nannyLoginPassword", password)
                    if(token!=" ")
                        put("deviceToken", token)
                }

        AndroidNetworking.post("https://www.uptodd.com/api/appusers/nanny")
            .addJSONObjectBody(loginCreds)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    if (response != null && response.get("status") == "Success") {


                        Log.d("login data",response.get("data").toString())
                        loginMethod = "EmailPassword"
                        if (((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "motherStage"
                            ) != "null"
                        )
                            isNewUser = false
                        if (((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "whichParent"
                            ) != "null"
                        )
                            isNewUser = false


                        uid =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getLong(
                                "id"
                            )
                                .toString()
                        kidsDob =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "kidsDob"
                            )
                        babyName =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "kidsName"
                            )
                        parentType =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "whichParent"
                            )
                        if (babyName == "null")
                            babyName = "baby"
                        profileImageUrl =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "profileImageUrl"
                            )
                        uid =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getLong(
                                "id"
                            )
                                .toString()
                        phoneNo =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "phoneno"
                            )
                        userName =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "name"
                            )

                        kidsDob =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "kidsDob"
                            )
                        kidsGender =
                            ((response.get("data") as JSONObject).getJSONObject("user")).getString(
                                "kidsGender"
                            )

                        if (kidsDob != "null")
                            babyDOB =
                                SimpleDateFormat(
                                    "yyyy-mm-dd",
                                    Locale.ENGLISH
                                ).parse(kidsDob)!!.time
                        tokenHeader =
                            (response.get("data") as JSONObject).getString("token")

                        Log.d("header token","$tokenHeader")
                        motherStage=
                            (response.get("data") as JSONObject).getJSONObject("user").getString(
                                "motherStage"
                            )
                        address=
                            (response.get("data") as JSONObject).getJSONObject("user").getString(
                                "address"
                            )

                        subscriptionStartDate=(response["data"] as JSONObject).getJSONObject("user")
                            .getString("subscriptionStartDate")
                        subsriptionEndDate=(response["data"] as JSONObject).getJSONObject("user")
                            .getString("subscriptionEndingDate")
                        //appAccessingDate=(response["data"] as JSONObject).getJSONObject("user")
                          //  .getString("appAccessEndingDate")
                        Log.d("start",subscriptionStartDate)
                        Log.d("end",subsriptionEndDate)

                        isNewUser = ((babyName=="null" || babyName==null)|| (kidsGender=="null" ||kidsGender==""))


                        val userInfo = UserInfo(
                            uid,userName,
                            address,
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
    fun getNPDetails(context: Context)
    {

        val uid = AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/nonPremiumAppusers/initialSetupDetails/${uid}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {
                        viewModelScope.launch {
                            if(response["data"].toString()=="null")
                            {
                                iSNPNew.value=true
                            }
                            else {
                                Log.d("data", response["data"].toString())

                                val nonPremiumAccount =
                                    AllUtil.getNonPAccount(response.get("data").toString())
                                UptoddSharedPreferences.getInstance(context)
                                    .saveNonPAccount(nonPremiumAccount)
                                nonPremiumAccount.anythingSpecial?.let {
                                    Log.d(
                                        "anythingTodos",
                                        it
                                    )
                                }
                                iSNPNew.value=false
                            }
                        }

                    } else {

                    }

                }

                override fun onError(error: ANError) {

                    Log.e("errorNonpremim", error.errorBody)
                    errorFromApiResponse.value=error.errorDetail
                }
            })
    }



    fun checkEmail() = loginId.value.isNullOrBlank()

    fun checkPassword() = sPassword.value.isNullOrBlank()

}