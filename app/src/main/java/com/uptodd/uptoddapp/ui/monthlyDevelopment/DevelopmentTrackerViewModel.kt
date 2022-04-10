package com.uptodd.uptoddapp.ui.monthlyDevelopment

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.all.All
import com.google.gson.Gson
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.AnswerModel
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.DevelopmentTracker
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.FormQuestionResponse
import com.uptodd.uptoddapp.ui.monthlyDevelopment.models.Response
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception

class DevelopmentTrackerViewModel(applicationContext:Application):AndroidViewModel(applicationContext) {

    var trackerList : MutableLiveData<DevelopmentTracker> = MutableLiveData()
    var errorResponse : MutableLiveData<Boolean> = MutableLiveData()
    var formQuestions=MutableLiveData<FormQuestionResponse>()
    val shouldShowForm= MutableLiveData<Boolean>()
    val formSubmitted=MutableLiveData<Boolean>()



    fun fetchTrackerResponse(context: Context){

        val userId = AllUtil.getUserId()

        AndroidNetworking.get("https://uptodd.com/api/appusers/previousTrackerResponses/$userId")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        Log.d("Response",response.toString() )
                        val devResponse = AllUtil.getDevelopmentTrackerResponse(response.toString())
                        trackerList.postValue(devResponse)
                        fetchQuestions()
                        shouldShowForm.postValue(devResponse.data.isTrackerFormOpen==1)
                    } catch (e:Exception) {
                        errorResponse.postValue(true)
                    }
                }

                override fun onError(error: ANError) {
                    errorResponse.postValue(true)
                    Log.i("error", error.errorBody)
                }
            })
    }
    fun fetchQuestions(){
        val userId = AllUtil.getUserId()

        AndroidNetworking.get("https://uptodd.com/api/appusers/trackerQuestions/$userId")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        Log.d("Response",response.toString() )
                        if(shouldShowForm.value == true) {
                            val queResponse =
                                AllUtil.getDevelopmentFormQuestions(response.toString())
                            formQuestions.postValue(queResponse)
                        }
                    } catch (e:Exception) {
                        errorResponse.postValue(true)
                    }
                }

                override fun onError(error: ANError) {
                    errorResponse.postValue(true)
                    Log.i("error", error.errorBody)
                }
            })
    }
    fun submitForm(response: ArrayList<Response>){

        val gson = Gson()
        val answers=AnswerModel(response)

        val jsonString = gson.toJson(answers)
       Log.d("answers response",jsonString)

        val userId = AllUtil.getUserId()
        AndroidNetworking.post("https://uptodd.com/api/appusers/trackerResponseV2/$userId")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .addJSONObjectBody(JSONObject(jsonString))
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        Log.d("Response Form submitted",response.toString() )
                        formSubmitted.postValue(true)

                    } catch (e:Exception) {
                        formSubmitted.postValue(false)
                    }
                }

                override fun onError(error: ANError) {
                    formSubmitted.postValue(false)
                    Log.i("error", error.errorBody)
                }
            })
    }



}