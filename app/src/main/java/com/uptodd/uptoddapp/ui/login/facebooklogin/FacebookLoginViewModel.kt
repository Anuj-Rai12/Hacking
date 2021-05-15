package com.uptodd.uptoddapp.ui.login.facebooklogin

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.logindetails.Explorers
import org.json.JSONObject

class FacebookLoginViewModel :ViewModel()
{
    var isUploadingExplorerData= MutableLiveData<Boolean>()
    var isExplorerDataUploaded=false
    fun uploadExplorerData(explorers: Explorers, token: String?)
    {
        val jsonObject= JSONObject()
        jsonObject.put("authId",explorers.uid)
        jsonObject.put("name",explorers.name)
        jsonObject.put("phoneNo",explorers.phone)
        jsonObject.put("profileImageUrl",explorers.profileImageUrl)
        jsonObject.put("deviceToken",token)


        AndroidNetworking.post("https://uptodd.com/api/explorers")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if(response!=null && response.get("status")=="Success")
                        isExplorerDataUploaded=true
                    isUploadingExplorerData.value=false
                    return
                }

                override fun onError(error: ANError?) {
                    isUploadingExplorerData.value=false
                    Log.d("div", "FacebookLoginViewModel L39 $error")
                    if (error!!.getErrorCode() != 0) {
                        Log.d("div", "onError errorCode : " + error.getErrorCode())
                        Log.d("div", "onError errorBody : " + error.getErrorBody())
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    }
                    return
                }

            })
    }
}