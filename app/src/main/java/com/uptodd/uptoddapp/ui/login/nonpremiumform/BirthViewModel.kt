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
import com.uptodd.uptoddapp.database.nonpremium.NonPremiumAccount
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.ArrayList

class BirthViewModel: ViewModel() {

    companion object
    {
        var npAcc:NonPremiumAccount=NonPremiumAccount()
    }
    var isLoadingDialogVisible = MutableLiveData<Boolean>()
    var jsonObject:MutableLiveData<JSONObject> = MutableLiveData()
    var isDataLoadedToDatabase = false
    var uid: String? = null
    var parent:String?=null


    init {
        if(jsonObject.value==null)
            jsonObject.value= JSONObject()
    }

    fun putUserId(userID:Long)
    {
        jsonObject.value?.put("userId",userID)

    }

    fun putWhichParent(parent:String)
    {
        jsonObject.value?.put("whichParent",parent)
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
    fun putMinutes(minutes: Int?)
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
        parent?.let { Log.d("whichParent", it) }
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
        npAcc.kidsDob?.let { putDob(it) }
        npAcc.anythingYouDo?.let { putAnything(it) }
        npAcc.name?.let { putName(it) }
        npAcc.anythingSpecial?.let { putSpecial(it) }
        npAcc.kidsName?.let { putBabyName(it) }
        npAcc.expectedMonthsOfDelivery?.let { putDelivery(it) }
        putMinutes(npAcc.minutesForBaby)
        npAcc.kidsToy?.let { putToys(it) }
        npAcc.majorObjective?.let { putObjective(it) }
        parent?.let { putWhichParent(it) }
        AndroidNetworking.post("https://uptodd.com/api/nonPremiumAppusers/initialSetup")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
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


        AndroidNetworking.put("https://www.uptodd.com/api/appusers/setup/{uid}")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
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