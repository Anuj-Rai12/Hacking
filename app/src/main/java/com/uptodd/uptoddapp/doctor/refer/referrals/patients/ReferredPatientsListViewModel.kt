package com.uptodd.uptoddapp.doctor.refer.referrals.patients

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


class ReferredPatientsListViewModel : ViewModel() {

    private var fullReferredListPatient = ArrayList<ReferredListItemPatient>()

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    private var _referredListPatient: MutableLiveData<ArrayList<ReferredListItemPatient>> = MutableLiveData()
    val referredListPatient: LiveData<ArrayList<ReferredListItemPatient>>
        get() = _referredListPatient

    init{
        _isLoading.value = 1
        getFullList()
    }

    fun loadFullList() {
        _referredListPatient.value = fullReferredListPatient
    }

    private fun getFullList(){
        Log.i("api", "calling api")
        AndroidNetworking.get("https://www.uptodd.com/api/referredpatients")
            .addQueryParameter("referredBy", "doctor")
            .addQueryParameter("referredById", AllUtil.getDoctorId().toString())
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.i("api", "on res ")
                    if (response != null) {
                        Log.i("api", "res nn")
                        val allReferrals = AllUtil.getAllPatientReferrals(response.get("data").toString())
                        allReferrals.forEach {
                            Log.i("item", it.toString())
                            it.referralDateValue = AllUtil.getTimeFromTimeStamp(it.referalDate)
                            if(it.registrationDate!=null && it.registrationDate.isNotEmpty())
                                it.registrationDateValue = AllUtil.getTimeFromTimeStamp(it.registrationDate)
//                            if(it.feedbackdates!=null && it.feedbackdates.isNotEmpty())
//                                it.feedbackdatesValue = AllUtil.getTimeFromTimeStamp(it.feedbackdates)
                        }
                        allReferrals.sortByDescending { it.referralDateValue }
                        fullReferredListPatient = allReferrals
                    }
                    _isLoading.value = 0
                }

                override fun onError(anError: ANError?) {
                    Log.i("api", "err $anError")
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    AllUtil.logApiError(anError)
                    _isLoading.value = -1
                }
            })

    }

    fun loadData(filters: HashMap<String, String> = HashMap()){
        val tempList = ArrayList<ReferredListItemPatient>()
        tempList.addAll(fullReferredListPatient.filter { item ->
            var all = true

            if(filters.containsKey("search"))
                all = all && (item.patientName.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)) || item.patientMail.toLowerCase(Locale.ROOT).contains(filters["search"]!!.toLowerCase(Locale.ROOT)))

            all = all && when{
                (filters.containsKey("successful") && !filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Referral Success")
                (!filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Referral Submitted") //Pending
                (!filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Referral Cancelled")
                (filters.containsKey("successful") && filters.containsKey("pending") && !filters.containsKey("failed")) -> (item.referralStatus == "Referral Success") || (item.referralStatus == "Referral Submitted")
                (filters.containsKey("successful") && !filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Referral Success") || (item.referralStatus == "Referral Cancelled")
                (!filters.containsKey("successful") && filters.containsKey("pending") && filters.containsKey("failed")) -> (item.referralStatus == "Referral Cancelled") || (item.referralStatus == "Referral Submitted")
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

//            if(filters.containsKey("paid"))
//                all = all && (item.isPaid == filters["paid"].toBoolean())
            all
        })
        _referredListPatient.value = ArrayList(tempList.sortedBy{ it.referralDateValue })
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateFromTime(time: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(time))
    }
}