package com.uptodd.uptoddapp.support.all.allsessions.bookaslot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.time.LocalDate


class BookASlotViewModel : ViewModel() {
    var expertList = ArrayList<String>()
    var expertId = ArrayList<Int>()

    lateinit var mStringArray: Array<String>

    var expertName = MutableLiveData<String>()
    var expertIdValue = MutableLiveData<Int>()

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var availableDates: ArrayList<LocalDate> = ArrayList()

    var apiError = ""

    var thisMonth = LocalDate.now().month.value
    var thisYear = LocalDate.now().year

    init{
        _isLoading.value = 1
        expertName.value = "Any"
        expertIdValue.value = -1
        getAllExpertList()
        Log.i("Month", thisMonth.toString())
    }

    private fun getAllExpertList(){
        AndroidNetworking.get("https://uptodd.com/api/appusers/sessions/experts/{userId}")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .addPathParameter("userId", AllUtil.getUserId().toString())
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.i("responseAll", response.toString())
                    if (response != null) {
                        val allTickets = AllUtil.getAllExperts(response.get("data").toString())
                        expertList.add("Select your previous doctor")
                        expertId.add(-1)
                        Log.d("div","BookASlotViewModel L57 $expertList \n $allTickets")
                        allTickets.forEach {
                            if(it.expertName!=null) {
                                expertList.add(it.expertName)
                                expertId.add(it.expertId)
                            }
                        }
                        mStringArray = Array(expertList.size) {
                            expertList[it]
                        }
                        Log.d("div","BookASlotViewModel L67 $expertList")
                        _isLoading.value = 0
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

    fun getAllExpertDates(){
        _isLoading.value = 1
        availableDates.clear()
        AndroidNetworking.get("https://uptodd.com/api/appuser/expertavailability")
            .addQueryParameter("month", thisMonth.toString())
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("response", response.toString())
                        if(thisMonth<=11){
                            val jsonObject = response.getJSONObject("data")
//                            Log.i("responseMonth", jsonObject.get(getMonthFromTestApi(thisMonth)).toString())
                            val thisMonthDates = AllUtil.getAllDates(jsonObject.get(thisMonth.toString()).toString())
                            addAllDates(thisMonthDates, thisMonth)
                            val nextMonthDates = AllUtil.getAllDates(jsonObject.get((thisMonth+1).toString()).toString())
                            addAllDates(nextMonthDates, thisMonth+1)
                        }
                        else{
                            val jsonObject = response.getJSONObject("data")
                            val thisMonthDates = AllUtil.getAllDates(jsonObject.get(thisMonth.toString()).toString())
                            addAllDates(thisMonthDates, thisMonth)
                            val nextMonthDates = AllUtil.getAllDates(jsonObject.get((1).toString()).toString())
                            addAllDates(nextMonthDates, 1)
                        }
                        _isLoading.value = 2
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

    private fun addAllDates(monthDates: ArrayList<Int>, month: Int) {
        monthDates.forEachIndexed { index, value ->
            if(value==1 && index+1<=LocalDate.of(thisYear, month, 1).lengthOfMonth()){
                availableDates.add(LocalDate.of(thisYear, month, index+1))
            }
        }
    }

    fun getSpecificExpertAvailability(id: Int) {
        _isLoading.value = 1
        availableDates.clear()
        AndroidNetworking.get("https://uptodd.com/api/appuser/expertavailability")
            .addQueryParameter("month", thisMonth.toString())
            .addQueryParameter("expertid", id.toString())
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("response", response.toString())
                        if(thisMonth<=11){
                            val jsonObject = response.getJSONObject("data")
//                            Log.i("responseMonth", jsonObject.get(getMonthFromTestApi(thisMonth)).toString())
                            val thisMonthDates = AllUtil.getAllDates("[" + jsonObject.get(getMonthFromTestApi(thisMonth)).toString() + "]")
                            addAllDates(thisMonthDates, thisMonth)
                            val nextMonthDates = AllUtil.getAllDates("[" + jsonObject.get(getMonthFromTestApi(thisMonth+1)).toString() + "]")
                            addAllDates(nextMonthDates, thisMonth+1)
                        }
                        else{
                            val jsonObject = response.getJSONObject("data")
                            val thisMonthDates = AllUtil.getAllDates(jsonObject.get(thisMonth.toString()).toString())
                            addAllDates(thisMonthDates, thisMonth)
                            val nextMonthDates = AllUtil.getAllDates(jsonObject.get((1).toString()).toString())
                            addAllDates(nextMonthDates, 1)
                        }
                        _isLoading.value = 2
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

    private fun getMonthFromTestApi(month: Int): String {
        return when(month){
            1-> "jan"
            2-> "feb"
            3-> "march"
            4-> "april"
            5-> "may"
            6-> "june"
            7-> "july"
            8-> "aug"
            9-> "sept"
            10-> "oct"
            11-> "nov"
            else -> "december"
        }
    }

    fun resetState() {
        _isLoading.value = 201
    }


}