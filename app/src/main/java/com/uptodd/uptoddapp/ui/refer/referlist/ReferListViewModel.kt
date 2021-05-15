package com.uptodd.uptoddapp.ui.refer.referlist

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ReferListViewModel :ViewModel()
{
    var token: String?=null
    var uid: String=""
    private lateinit var fullReferredList: ArrayList<ReferredListItemPatient>

    private var _referredList: MutableLiveData<ArrayList<ReferredListItemPatient>> = MutableLiveData()
    val referredList: LiveData<ArrayList<ReferredListItemPatient>>
        get() = _referredList

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    fun loadFullList() {
        _referredList.value = fullReferredList
    }

    fun getFullList() {
        _isLoading.value=1
        AndroidNetworking.get("https://uptodd.com/api/referredpatients")
            .addQueryParameter("referredBy", "patient")
            .addQueryParameter("referredById", uid)
            .addHeaders("Authorization","Bearer $token")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div","ReferListViewModel L58 $response")
                    if (response != null) {
                        if(response.getString("status") == "Success") {
                            val allReferrals = AllUtil.getAllPatientReferrals(response.get("data").toString())
                            allReferrals.forEach {
                                Log.i("item", it.toString())
                                it.referralDateValue = AllUtil.getTimeFromTimeStamp(it.referalDate)
                                if (it.registrationDate != null && it.registrationDate.isNotEmpty())
                                    it.registrationDateValue =
                                        AllUtil.getTimeFromTimeStamp(it.registrationDate)
                            }
                            allReferrals.sortByDescending { it.referralDateValue }
                            fullReferredList = allReferrals
                        }
                    }
                    _isLoading.value = 0
                }

                //"id":43,"referredBy":"patient","referredById":109,"patientName":"divyanshutw@gmail.com","patientMail":"divyanshutw@gmail.com",
                // "patientPhone":"9198905391","patientWhatsapp":null,"babyGender":null,"babyDOB":null,"referalDate":"2021-01-20 15:44:15",
                // "registrationDate":null,"enrolledDuration":null,"amountSubmitted":null,"referralStatus":"Pending",
                // "feedback":null,"feedbackdates":null,"ip":null

                override fun onError(anError: ANError?) {
                    Log.i("api", "err $anError")
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -1
                }
            })
    }

    fun loadData(filters: HashMap<String, String> = HashMap()){
        val tempList = ArrayList<ReferredListItemPatient>()
        tempList.addAll(fullReferredList.filter { item ->
            var all = true

            if(filters.containsKey("search"))
                all = all && (item.patientName!!.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)) || item.patientMail!!.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)))

            all = all && when{
                (filters.containsKey("successful") && !filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Success")
                (!filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Pending")
                (!filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Cancelled")
                (filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Success") || (item.referralStatus == "Pending")
                (filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Success") || (item.referralStatus == "Cancelled")
                (!filters.containsKey("successful") && filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Cancelled") || (item.referralStatus == "Pending")
                else -> true
            }

            if(filters.containsKey("start_date")) {
                val filterTime = filters["start_date"]!!.toLong()
                all = all && item.referralDateValue >= filterTime
            }
            if(filters.containsKey("end_date")) {
                val filterTime = filters["end_date"]!!.toLong()
                Log.i("div", "ReferListViewModel ${item.referralDateValue} vs end of $filterTime")
                all = all && item.referralDateValue <= filterTime
            }

            all
        })
        _referredList.value = ArrayList(tempList.sortedByDescending { it.referralDateValue })
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFromTime(time: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(time))
    }

}