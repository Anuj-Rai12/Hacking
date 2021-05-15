package com.uptodd.uptoddapp.doctor.refer.referrals.doctor

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.referrals.ReferredListItemDoctor
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class ReferredDoctorsListViewModel : ViewModel() {

    private var fullReferredListDoctor = ArrayList<ReferredListItemDoctor>()

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    private var _referredListDoctor: MutableLiveData<ArrayList<ReferredListItemDoctor>> = MutableLiveData()
    val referredListDoctor: LiveData<ArrayList<ReferredListItemDoctor>>
        get() = _referredListDoctor

    var apiError = ""

    init{
        _isLoading.value = 1
        getFullList()
    }

    fun loadFullList() {
        _referredListDoctor.value = fullReferredListDoctor
    }

    private fun getFullList() {
        AndroidNetworking.get("https://uptodd.com/api/doctor/referreddoctors/{doctorId}")
            .addPathParameter("doctorId", AllUtil.getDoctorId().toString())
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        val allReferrals = AllUtil.getAllDoctorReferrals(response.get("data").toString())
                        allReferrals.forEach {
                            it.referralDateValue = AllUtil.getTimeFromTimeStamp(it.referralDate)
                            if(it.registrationDate!=null && it.registrationDate.isNotEmpty())
                                it.registrationDateValue = AllUtil.getTimeFromTimeStamp(it.registrationDate)
                        }
                        allReferrals.sortByDescending { it.referralDateValue }
                        fullReferredListDoctor = allReferrals
                    }
                    _isLoading.value = 0
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

    fun loadData(filters: HashMap<String, String> = HashMap()){
        val tempList = ArrayList<ReferredListItemDoctor>()
        tempList.addAll(fullReferredListDoctor.filter { item ->
            var all = true

            if(filters.containsKey("search"))
                all = all && (item.name.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)) || item.mail.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)))

            all = all && when{
                (filters.containsKey("successful") && !filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Success")
                (!filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Pending") //Pending
                (!filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Cancelled")
                (filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Success") || (item.referralStatus == "Pending")
                (filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Success") || (item.referralStatus == "Cancelled")
                (!filters.containsKey("successful") && filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Cancelled") || (item.referralStatus == "Pending")
                else -> true
            }

            if(filters.containsKey("start_date")) {
                val filterTime = filters["start_date"]!!.toLong()
                Log.i("filterTime", "${item.referralDateValue} vs start of $filterTime")
                all = all && item.referralDateValue >= filterTime
            }
            if(filters.containsKey("end_date")) {
                val filterTime = filters["end_date"]!!.toLong()
                Log.i("filterTime", "${item.referralDateValue} vs end of $filterTime")
                all = all && item.referralDateValue <= filterTime
            }
//
//            if(filters.containsKey("paid"))
//                all = all && (item.isPaid == filters["paid"].toBoolean())
            all
        })
        _referredListDoctor.value = ArrayList(tempList.sortedBy { it.referralDateValue })
    }

    //Returns date from time in millis
    @SuppressLint("SimpleDateFormat")
    fun getDateFromTime(time: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(time))
    }
}