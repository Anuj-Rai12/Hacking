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

    var uid: String? = null

    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var isDataLoadedToDatabase = false


    fun insertAddressDetails(address:String) {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("address",address)

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