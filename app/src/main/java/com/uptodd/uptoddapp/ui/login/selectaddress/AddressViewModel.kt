package com.uptodd.uptoddapp.ui.login.selectaddress

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject


class AddressViewModel() : ViewModel() {

    var babyName: String?=""
    var uid: String? = null
    var babyGender:String?=""
    var stage:String?=""


    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var isDataLoadedToDatabase = false

    companion object
    {
        var isGenderName=false

    }


    fun insertAddressDetails(address:String) {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("address",address)
        if(isGenderName)
        {
            jsonObject.put("motherStage", stage)
            jsonObject.put("kidsName", babyName)
            jsonObject.put("kidsGender", babyGender)
        }

        AndroidNetworking.put("https://uptodd.com/api/appusers/setup/{uid}")
            .addPathParameter("uid", uid)
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "AddressViewModel L57 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "AddressViewModel L59 ${response.get("status")}")
                        isDataLoadedToDatabase = true
                        isLoadingDialogVisible.value = false

                    }
                }

                override fun onError(anError: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "AddressViewModel L67 ${anError!!.message}")
                }

            })
    }


}