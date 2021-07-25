package com.uptodd.uptoddapp.support.all.allsessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.support.Sessions
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class AllSessionsViewModel : ViewModel() {

    private var _sessions: MutableLiveData<ArrayList<Sessions>> = MutableLiveData()
    val sessions: LiveData<ArrayList<Sessions>>
        get() = _sessions

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    init {
        _isLoading.value = 1
        getAllSessions()
    }

    fun getAllSessions(){
        _isLoading.value = 1
        AndroidNetworking.get("https://www.uptodd.com/api/appusers/sessions/{userId}")
            .addPathParameter("userId", AllUtil.getUserId().toString())
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        if (response.getString("status") != "Success") {
                            apiError = response.getString("message")
                            _isLoading.value = -1
                        } else {
                            val allSessions = AllUtil.getAllSessions(response.get("data").toString())
                            allSessions.forEach {
                                it.sessionBookingDateValue = AllUtil.getTimeFromTimeStamp(it.sessionBookingDate)
                            }
                            _sessions.value = ArrayList(allSessions.sortedByDescending{ it.sessionBookingDateValue })
                            _isLoading.value = 0
                        }
                    }
                    else{
                        apiError = "Unknown Error!"
                        _isLoading.value = -1
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -1
                    AllUtil.logApiError(anError)
                }
            })
    }

    fun rateSession(session: Sessions, message: String, index: Int) {
        val jsonObject = JSONObject()
        if(message.isNotEmpty())
            jsonObject.put("sessionFeedback", message)
        jsonObject.put("sessionRating", session.sessionRating)
        _isLoading.value = 11

        AndroidNetworking.put("https://www.uptodd.com/api/appusers/sessions/{sessionID}")
            .addPathParameter("sessionId", session.id.toString())
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        _isLoading.value = 10
                        val tempSession = _sessions.value!!
                        tempSession[index] = session
                        _sessions.value = tempSession
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -1
                }
            })
    }
}