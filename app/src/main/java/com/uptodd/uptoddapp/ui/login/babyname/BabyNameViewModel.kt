package com.uptodd.uptoddapp.ui.login.babyname

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject


class BabyNameViewModel() : ViewModel() {

    var loginMethod: String? = null
    var parentType: String? = null
    var stage: String? = "born"
    var uid: String? = null
    var babyGender: String? = null
    var babyName: String? = null
    var email: String? = null

    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var isDataLoadedToDatabase = false

    fun insertLoginDetails() {
        val jsonObject = JSONObject()
        jsonObject.put("whichParent", parentType)
        jsonObject.put("motherStage", stage)
        jsonObject.put("kidsName", babyName)
        jsonObject.put("kidsGender", babyGender)

        AndroidNetworking.put("https://www.uptodd.com/api/appusers/setup/{uid}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .addPathParameter("uid", uid)
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "BabyNameViewModel L57 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "BabyNameViewModel L59 ${response.get("status")}")
                        isDataLoadedToDatabase = true
                        isLoadingDialogVisible.value = false

                    }
                }

                override fun onError(anError: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "BabyNameViewModel L67 ${anError!!.message}")
                }

            })


    }


}