package com.uptodd.uptoddapp.ui.account.account

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.account.Account
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class AccountViewModel(application: Application, val uid: String?, val token: String?) :
    AndroidViewModel(application) {


    private val accountPreference = UptoddSharedPreferences.getInstance(application)

    //    private val database = UptoddDatabase.getInstance(application)
    private val accountRepository = AccountRepository(accountPreference)

    var isRepositoryEmpty = MutableLiveData<Boolean>()

    init {
        if (uid != null) {
            viewModelScope.launch {
                accountRepository.refreshAccountDetails(uid, token)
            }
        }
    }

    /*fun fetchDetails()
    {
        val account=Account(1,"https://www.linkpicture.com/q/profile_8.png","Div","uptodd@gmail.com",
            "646464534646","dbchjsjhvcsdg",4,10,true,"qwerty","asdf34?")
        _currentAccount.value=account
    }*/

    val currentAccount = accountRepository.accountDetails.map {
        it
    }

    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var isDataLoadedToDatabase = false

    var imageBitmap: Bitmap? = null            //null if profile image is not updated
    var imagePath: String? = null               //null if profile image is not updated
    var imageUri: Uri? = null               //null if profile image is not updated
    var imageFile: File? = null               //null if profile image is not updated
    var isSavedToLocal: Boolean = false


    fun saveDetails(account: Account) {

        AndroidNetworking.upload("https://www.uptodd.com/api/appusers/editprofile/{userId}")
            .addPathParameter("userId", uid!!)
            .addHeaders("Authorization", "Bearer $token")
            .addMultipartParameter("email", account.email)
            .addMultipartParameter("address", account.address)
            .addMultipartParameter("phoneno", account.phone)
            .addMultipartParameter("kidsName", account.kidsName)
            .addMultipartFile("profile", imageFile)
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->
                // do anything with progress
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    // do anything with response
                    Log.d("div", "AccountViewModel L79 $response")
                    if (response.get("status") == "Success") {
                        isDataLoadedToDatabase = true
                        viewModelScope.launch {
                            accountRepository.refreshAccountDetails(uid, token)
                        }
                    }
                    isLoadingDialogVisible.value = false
                }

                override fun onError(error: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "AccountViewModel L67 $error")
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


    var isNannyEnabled = false
    var isNannyUpdated = false
    var isNannyUpdating = MutableLiveData<Boolean>()

    fun setUpNannyMode(nannyId: String, nannyPassword: String) {
        if (uid == null) return
        val jsonObject = JSONObject()
        jsonObject.put("nannyLoginId", nannyId)
        jsonObject.put("nannyLoginPassword", nannyPassword)
        val date = LocalDate.now().toString()
        jsonObject.put("updateTime", date)


        AndroidNetworking.put("https://www.uptodd.com/api/appusers/nannysetup/{userId}")
            .addPathParameter("userId", uid!!)
            .addHeaders("Authorization", "Bearer $token")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "AccountViewModel L115 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "AccountViewModel L117 ${response.get("status")}")
                        viewModelScope.launch {
                            accountRepository.refreshAccountDetails(uid, token)
                        }
                        isNannyEnabled = true
                        isNannyUpdated = true
                        isNannyUpdating.value = false
                    }
                }

                override fun onError(error: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "AccountViewModel L67 $error")
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