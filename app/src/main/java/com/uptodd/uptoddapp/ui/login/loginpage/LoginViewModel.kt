package com.uptodd.uptoddapp.ui.login.loginpage

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthCredential
import com.google.firebase.iid.FirebaseInstanceId
import com.uptodd.uptoddapp.database.logindetails.Explorers
import com.uptodd.uptoddapp.database.logindetails.UserInfo
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.UpToddDialogs
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class LoginViewModel : ViewModel() {

    var incorrectEmailMsg: String = ""
    var incorrectPasswordMsg: String = ""

    var authenticatedUserLiveData: LiveData<Explorers>? = null
    private var authRepository = AuthRepository()

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

    var emailId = MutableLiveData<String>()
    var sPassword = MutableLiveData<String>()

    var incorrectEmail = MutableLiveData<Boolean>()
        private set
    var incorrectPassword = MutableLiveData<Boolean>()
        private set

    var loginResponse = MutableLiveData<UserInfo>()
    var iSNPNew=MutableLiveData<Boolean>()
    var showLoginProgress = MutableLiveData<Boolean>(false)
    var enableInput = MutableLiveData<Boolean>(true)

    fun signInWithGoogle(googleAuthCredential: AuthCredential?) {
        authenticatedUserLiveData = authRepository.firebaseSignInWithGoogle(googleAuthCredential)
    }

    fun signOutFromGoogle(googleSignInClient: GoogleSignInClient) {
        authRepository.googleSignOut(googleSignInClient)
    }

    var isValidatingEmailPassword = MutableLiveData<Boolean>()
    var isEmailPasswordCorrect = false
    var errorFromApiResponse = MutableLiveData<String>()
        private set

    var isUploadingExplorerData = MutableLiveData<Boolean>()
    var isExplorerDataUploaded = false
    fun uploadExplorerData(explorers: Explorers, token: String?) {
        val jsonObject = JSONObject()
        jsonObject.put("authId", explorers.uid)
        jsonObject.put("name", explorers.name)
        jsonObject.put("phoneNo", explorers.phone)
        jsonObject.put("profileImageUrl", explorers.profileImageUrl)
        jsonObject.put("deviceToken", token)

        AndroidNetworking.post("https://uptodd.com/api/explorers")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null && response.get("status") == "Success")
                        isExplorerDataUploaded = true
                    isUploadingExplorerData.value = false
                }

                override fun onError(error: ANError?) {
                    isUploadingExplorerData.value = false
                    Log.d("div", "LoginViewModel L120 $error")
                    if (error!!.errorCode != 0) {
                        Log.d("div", "onError errorCode : " + error.errorCode)
                        Log.d("div", "onError errorBody : " + error.errorBody)
                        Log.d("div", "onError errorDetail : " + error.errorDetail)
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.errorDetail)
                    }
                }

            })
    }


    fun beginLogin() {

        toggleUI()

        val emailEmpty = checkEmail()
        val passwordEmpty = checkPassword()

        if (emailEmpty || passwordEmpty) {
            incorrectEmailMsg = "Email cannot be empty"
            incorrectPasswordMsg = "Password Cannot be empty"
            incorrectEmail.value = emailEmpty
            incorrectPassword.value = passwordEmpty
            toggleUI()
            return
        }

        login()

    }

    fun checkEmail() = emailId.value.isNullOrBlank()

    fun checkPassword() = sPassword.value.isNullOrBlank()

    fun toggleUI() {
        showLoginProgress.value = !showLoginProgress.value!!
        enableInput.value = !enableInput.value!!
    }


    fun login() {
        var token: String
        val email = requireNotNull(emailId.value)
        val password = requireNotNull(sPassword.value)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                token = if (!task.isSuccessful) {
                    " "
                } else
                    task.result.token

                val loginCreds = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    if(token!=" ")
                    put("deviceToken", token)
                }

                AndroidNetworking.post("https://www.uptodd.com/api/appusers")
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
                                appAccessingDate=(response["data"] as JSONObject).getJSONObject("user")
                                    .getString("appAccessEndingDate")
                                Log.d("start",subscriptionStartDate)
                                Log.d("end",subsriptionEndDate)

                                isNewUser = ((babyName=="null" || babyName==null)|| (kidsGender=="null" ||kidsGender==""))


                                val info = UserInfo(
                                    uid,
                                    userName,address,
                                    isNewUser,
                                    "Normal",
                                    email,
                                    loginMethod,
                                    kidsDob,
                                    babyName,
                                    babyDOB,
                                    profileImageUrl,
                                    tokenHeader,
                                    parentType,
                                    true,
                                    System.currentTimeMillis(),
                                    tokenHeader,
                                    true
                                )
                                loginResponse.value = info
                                toggleUI()
                            }
                        }

                        override fun onError(error: ANError?) {

                            if (error!!.errorCode != 0) {
                                val errorRes =
                                    error.errorBody.substring(29, error.errorBody.length - 14)

                                if (errorRes == "Incorrect Credentials") {
                                    incorrectEmailMsg = "Incorrect Email"
                                    incorrectPasswordMsg = "Incorrect Password"
                                    incorrectEmail.value = true
                                    incorrectPassword.value = true
                                }

                                Log.d("div", "onError errorDetail : " + error.errorBody)
                                errorFromApiResponse.value = errorRes
                            } else {
                                // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                Log.d("div", "onError errorDetail : " + error.errorDetail)
                            }

                            toggleUI()
                        }
                    })
            })
    }
    fun getNPDetails(context: Context)
    {
        toggleUI()
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
                    toggleUI()
                }

                override fun onError(error: ANError) {
                    toggleUI()
                    Log.e("errorNonpremim", error.errorBody)
                    errorFromApiResponse.value=error.errorDetail
                }
            })
    }
}