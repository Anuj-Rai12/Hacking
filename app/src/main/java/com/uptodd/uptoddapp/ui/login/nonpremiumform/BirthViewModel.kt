package com.uptodd.uptoddapp.ui.login.nonpremiumform

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uptodd.uptoddapp.api.getUserId
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.nonpremium.NonPremiumAccount
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.ArrayList

class BirthViewModel: ViewModel() {

    var nonPremiumAccount:NonPremiumAccount?=null

    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var jsonObject:MutableLiveData<JSONObject> = MutableLiveData()
    var isDataLoadedToDatabase = false
    var uid: String? = null


    init {
        jsonObject.value= JSONObject()
    }

    fun putUserId(userID:Long)
    {
        jsonObject.value?.put("userId",userID)

    }

    fun putMotherStage(stage:String)
    {
        jsonObject.value?.put("motherStage", stage)
    }
    fun putName(name:String)
    {

        jsonObject.value?.put("name", name)
    }
    fun putBabyName(name: String)
    {
        jsonObject.value?.put("kidsName", name)
    }

    fun putDob(dob: String)
    {
        jsonObject.value?.put("kidsDob",dob)

    }
    fun putToys(toys:String)
    {
        jsonObject.value?.put("kidsToy",toys)
    }
    fun putMinutes(minutes:String)
    {
        jsonObject.value?.put("minutesForBaby",minutes)
    }
    fun putSpecial(special:String)
    {
        jsonObject.value?.put("anythingSpecial",special)
    }
    fun putObjective(objective:String)
    {
        jsonObject.value?.put("majorObjective",objective)

    }
    fun putDelivery(delivery:String)
    {
        jsonObject.value?.put("expectedMonthsOfDelivery",delivery)
    }
    fun putAnything(any:String)
    {
        jsonObject.value?.put("anythingYouDo",any)
    }

    fun  initialSetup(context:Context) {

        val json = jsonObject.value
        val gson = Gson()
        val type: Type = object : TypeToken<NonPremiumAccount>() {}.type

        val stage=UptoddSharedPreferences.getInstance(context).getStage()
        if(stage=="pre birth" ||stage=="prenatal"){
            putBabyName("")
            putDob("")
        }
        else
        {
            putDelivery("")
           putAnything("")
        }
        putMotherStage(stage!!)
        putUserId(getUserId(context)!!)
        AndroidNetworking.post("https://uptodd.com/api/nonPremiumAppusers/initialSetup")
            .addJSONObjectBody(json)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "BirthViewModel L57 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "BirthViewModel L59 ${response.get("status")}")

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

    fun insertDobDetails(dob:String) {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("kidsDob",dob)


        AndroidNetworking.put("https://uptodd.com/api/appusers/setup/{uid}")
            .addPathParameter("uid", uid)
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "BirthViewModel L57 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "BirthViewModel L59 ${response.get("status")}")
                        isDataLoadedToDatabase = true
                        isLoadingDialogVisible.value = false

                    }
                }

                override fun onError(anError: ANError?) {
                    isLoadingDialogVisible.value = false
                    Log.d("div", "BirthViewModel L67 ${anError!!.message}")
                }

            })
    }



}