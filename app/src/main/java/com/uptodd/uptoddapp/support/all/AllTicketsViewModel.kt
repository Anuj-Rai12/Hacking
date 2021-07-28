package com.uptodd.uptoddapp.support.all

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.support.Ticket
import com.uptodd.uptoddapp.utilities.AllUtil
import com.uptodd.uptoddapp.utilities.AllUtil.Companion.getUserId
import org.json.JSONObject


class AllTicketsViewModel : ViewModel() {

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    private var _allTickets = ArrayList<Ticket>()

    private var _tickets: MutableLiveData<ArrayList<Ticket>> = MutableLiveData()
    val tickets: LiveData<ArrayList<Ticket>>
        get() = _tickets

    var apiError = ""

    init {
        _isLoading.value=0
    }

    fun init()
    {
        _isLoading.value=1
        getAllTickets()
    }

    fun getAllTickets(){
        _isLoading.value = 1
        AndroidNetworking.get("https://www.uptodd.com/api/appuser/support/{userId}")
            .addPathParameter("userId", getUserId().toString())
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        val allTickets = AllUtil.getAllTickets(response.get("data").toString())
                        allTickets.forEach {
                            it.time = AllUtil.getTimeFromTimeStamp(it.ticketCreationDate)
                        }
                        _allTickets = allTickets
                    }
                    _isLoading.value = 0
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

    fun sortArray(type: String){
        _tickets.value = ArrayList(_allTickets.filter { it.type == type })
        _tickets.value = ArrayList(_tickets.value!!.sortedByDescending { it.time })
    }

    fun addNewTicket(ticket: Ticket){
        _tickets.value?.add(ticket)
//        Log.i("ticket", "added: ${ticket.ticketTitle} of id ${ticket.ticketID}")
    }

    fun updateTicket(ticket: Ticket, index: Int) {
        val tempTicket = _tickets.value!!
        tempTicket[index] = ticket
        _tickets.value = tempTicket
    }

    fun sendRating(ticket: Ticket, index: Int ,message: String) {
        val jsonObject = JSONObject()
        jsonObject.put("ticketNumber", ticket.ticketNumber)
        jsonObject.put("customerFeedback", message)
        jsonObject.put("rating", ticket.rating)
        _isLoading.value = 11

        AndroidNetworking.put("https://www.uptodd.com/api/appuser/support/")
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        _isLoading.value = 10
                        updateTicket(ticket, index)
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