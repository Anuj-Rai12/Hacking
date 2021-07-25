package com.uptodd.uptoddapp.support.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

class ViewTicketViewModel : ViewModel() {

    private var _messages = MutableLiveData<ArrayList<TicketMessage>>()
    val messages: LiveData<ArrayList<TicketMessage>>
        get() = _messages

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    private var _reopen: MutableLiveData<Boolean> = MutableLiveData()
    val reopen: LiveData<Boolean>
        get() = _reopen

    private var _close: MutableLiveData<Boolean> = MutableLiveData()
    val close: LiveData<Boolean>
        get() = _close

    private var _apiCalled: MutableLiveData<Boolean> = MutableLiveData()
    val apiCalled: LiveData<Boolean>
        get() = _apiCalled

    var apiError = ""

    init {
        _isLoading.value = 1
        _reopen.value = false
        _close.value = false
        _apiCalled.value = false
    }

    fun saveData(){
        viewModelScope.launch {

        }
        _apiCalled.value = false
    }

    fun getAllMessages(ticket: Ticket) {
        AndroidNetworking.get("https://uptodd.com/api/appuser/support")
            .addQueryParameter("ticketNumber", ticket.ticketNumber)
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        val jsObject = response.get("data") as JSONObject
                        val allMessages = AllUtil.getAllTicketMessages(
                            jsObject.get("messages").toString()
                        )
                        allMessages.forEach {
                            it.time = AllUtil.getTimeFromTimeStamp(it.timestamp)
                            it.isSenderValue = when (it.sender) {
                                "user" -> true
                                else -> false
                            }
                        }
                        _messages.value = allMessages
                        _apiCalled.value = true
                    }
                    _isLoading.value = 0
                }

                override fun onError(anError: ANError?) {
                    if(anError!!.errorCode==0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -1
                }
            })
    }

    fun updateReopen(){
        _reopen.value = false
    }

    fun updateClose(){
        _close.value = false
    }

    fun reopenTicket(message: String, ticket: Ticket){
        val jsonObject = JSONObject()
        jsonObject.put("ticketNumber", ticket.ticketNumber)
        jsonObject.put("message", message)
        jsonObject.put("sender", "user")
        jsonObject.put("ticketReopen", 1)

        AndroidNetworking.post("https://uptodd.com/api/appusers/support/newmessage")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if(response.get("status") == "Success"){
                        val tempMessages = _messages.value!!
                        tempMessages.add(TicketMessage(message, System.currentTimeMillis(), "user", true))
                        _messages.value = tempMessages
                        _reopen.value = true
                    }
                }

                override fun onError(error: ANError) {
                    // handle error
                }
            })
    }

    fun sendMessage(message: String, ticket: Ticket){

        val jsonObject = JSONObject()
        jsonObject.put("ticketNumber", ticket.ticketNumber)
        jsonObject.put("message", message)
        jsonObject.put("sender", "user")
        jsonObject.put("ticketReopen", 0)

        AndroidNetworking.post("https://uptodd.com/api/appusers/support/newmessage")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if(response.get("status") == "Success"){
                        val tempMessages = _messages.value!!
                        tempMessages.add(TicketMessage(message, System.currentTimeMillis(), "user", true))
                        _messages.value = tempMessages
                    }
                }

                override fun onError(error: ANError) {
                    // handle error
                }
            })

    }

    fun justCloseTicket(ticketNumber: String) {
        val jsonObject = JSONObject()
        jsonObject.put("ticketNumber", ticketNumber)
        jsonObject.put("rating", -1)
        jsonObject.put("closeTicket", 1)
        _isLoading.value = 21

        AndroidNetworking.put("https://uptodd.com/api/appuser/support")
            .addJSONObjectBody(jsonObject)
            .addHeaders("Content-Type", "application/json")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("line", "167 -> $response")
                        _isLoading.value = 20
                        _close.value = true
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    Log.e("apiError", "${anError.errorDetail} / ${anError.errorCode} / ${anError.errorBody} / ${anError.response}")
                    _isLoading.value = -1
                }
            })
    }

    fun closeTicketAndSubmitRating(ticket: Ticket, message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("ticketNumber", ticket.ticketNumber)
        jsonObject.put("rating", ticket.rating)
        jsonObject.put("closeTicket", "1")
        _isLoading.value = 11

        if(message.isNotEmpty())
            jsonObject.put("customerFeedback", message)

        AndroidNetworking.put("https://uptodd.com/api/appuser/support")
            .addJSONObjectBody(jsonObject)
            .addHeaders("Content-Type", "application/json")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("line", "198 -> $response")
                        _isLoading.value = 10
                        _close.value = true
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    Log.e("apiError", "${anError.errorDetail} / ${anError.errorCode} / ${anError.errorBody} / ${anError.response}")
                    _isLoading.value = -1
                }
            })
    }

}