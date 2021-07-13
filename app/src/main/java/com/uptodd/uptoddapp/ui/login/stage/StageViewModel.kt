package com.uptodd.uptoddapp.ui.login.stage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject

class StageViewModel() : ViewModel() {

    var loginMethod: String? = null
    var parentType: String? = null
    var stage: String = "postnatal"
    var uid: String? = null

    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var isDataLoadedToDatabase = false


    fun insertLoginDetails() {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("whichParent", parentType)
        jsonObject.put("motherStage", stage)
        jsonObject.put("kidsName", null)
        jsonObject.put("kidsGender", null)

        AndroidNetworking.put("https://uptodd.com/api/appusers/setup/{uid}")
            .addPathParameter("uid", uid)
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "StageViewModel L57 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "StageViewModel L59 ${response.get("status")}")
                        isDataLoadedToDatabase = true
                        isLoadingDialogVisible.value = false

                    }
                }

                override fun onError(anError: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "StageViewModel L67 ${anError!!.message}")
                }

            })
    }


}