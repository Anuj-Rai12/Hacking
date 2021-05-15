package com.uptodd.uptoddapp.support.create

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject

class CreateTicketViewModel : ViewModel() {

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    init {
        _isLoading.value = 0
    }

    fun submitNewTicket(title: String, message: String, supportType: String){
        _isLoading.value = 1
        var support = ""
        if(supportType=="Expert Suggestion")
            support = "expert"
        else
            support = "support"
        val newTicket = Ticket(
            AllUtil.getUserId(),
            support+AllUtil.getUserId()+System.currentTimeMillis(),
            title,
            supportType,
            message
        )

        val jsonObject = JSONObject()
        jsonObject.put("userId", AllUtil.getUserId())
        jsonObject.put("ticketNumber", newTicket.ticketNumber)
        jsonObject.put("subject", newTicket.subject)
        jsonObject.put("type", newTicket.type)
        jsonObject.put("message", message)

        AndroidNetworking.post("https://uptodd.com/api/appuser/support")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if(response.get("status") == "Success"){
                        _isLoading.value = 2
                    }
                    else{
                        Log.e("Error", "Unknown")
                        _isLoading.value = -2
                    }
                }

                override fun onError(error: ANError) {
                    // handle error
                    apiError = error.toString()
                    _isLoading.value = -1
                }
            })
    }
}