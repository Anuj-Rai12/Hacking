package com.uptodd.uptoddapp.doctor.refer.referrals

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uptodd.uptoddapp.database.referrals.ReferredListItemDoctor
import com.uptodd.uptoddapp.database.referrals.ReferredListItemPatient
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class ReferralDetailsViewModel : ViewModel() {

    private var _babyVisibility = MutableLiveData<Int>()
    val babyVisibility: LiveData<Int>
        get() = _babyVisibility

    private var _doctorVisibility = MutableLiveData<Int>()
    val doctorVisibility: LiveData<Int>
        get() = _doctorVisibility


    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var referredPersonDoctor = ReferredListItemDoctor()
    var referredPersonPatient = ReferredListItemPatient()

//    private var _referralName = MutableLiveData<String>()
//    val referralName: LiveData<Int>
//        get() = _babyVisibility
    
    var layout1Text1Text = MutableLiveData<String>()
    var layout2Text2Text = MutableLiveData<String>()
    var layout3Text3Text = MutableLiveData<String>()

    var referralId = MutableLiveData<Int>()
    var referralName = MutableLiveData<String>()
    var referralEmail = MutableLiveData<String>()
    var referralPhone = MutableLiveData<String>()
    var referralCity = MutableLiveData<String>()
    var referralDate = MutableLiveData<String>()
    var referralStatus = MutableLiveData<String>()
    var referralRegistrationDate = MutableLiveData<String>()
    var babyName = MutableLiveData<String>()
    var babyDOB = MutableLiveData<String>()
    var layout1Field1 = MutableLiveData<String>()
    var layout2Field1 = MutableLiveData<String>()
    var layout3Field1 = MutableLiveData<String>()

    var apiError = ""

    fun submitNewDoctorDetails() {
        val jsonObject = JSONObject()
        jsonObject.put("id", referredPersonDoctor.id)
        jsonObject.put("mail", referralEmail.value)
        jsonObject.put("phone", referralPhone.value)
        jsonObject.put("city", referralCity.value)
        _isLoading.value = 11


        AndroidNetworking.put("https://uptodd.com/api/doctor/referreddoctors")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .addJSONObjectBody(jsonObject)
            .addHeaders("Content-Type", "application/json")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("line", "167 -> $response")
                        _isLoading.value = 10
                        Log.i("response", response.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    Log.i("error", "${anError.errorDetail} / ${anError.errorCode} / ${anError.errorBody} / ${anError.response}")
                    _isLoading.value = -1
                }
            })
    }

    fun submitNewPatientDetails(){
        val jsonObject = JSONObject()
        jsonObject.put("id", referredPersonPatient.id)
        jsonObject.put("mail", referralEmail.value)
        jsonObject.put("phone", referralPhone.value)
        _isLoading.value = 11

        Log.i("apiError", "Sending: $jsonObject")

        AndroidNetworking.put("https://www.uptodd.com/api/referredpatients")
            .addJSONObjectBody(jsonObject)
            .addHeaders("Content-Type", "application/json")
            .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        _isLoading.value = 10
                        Log.i("response", response.toString())
                    }
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    AllUtil.logApiError(anError)
                    _isLoading.value = -1
                }
            })
    }

    fun getReferralDetails(doctor: Boolean, referredPersonId: Int){
        if(doctor){
            AndroidNetworking.get("https://www.uptodd.com/api/doctor/referral/{referredId}")
                .addPathParameter("referredId", referredPersonId.toString())
                .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            val gson = Gson()
                            val type: Type = object : TypeToken<ReferredListItemDoctor?>() {}.type
                            referredPersonDoctor = gson.fromJson(response.get("data").toString(), type) as ReferredListItemDoctor
                            referredPersonDoctor.referralDateValue = AllUtil.getTimeFromTimeStamp(referredPersonDoctor.referralDate)
                            if(referredPersonDoctor.registrationDate!=null && referredPersonDoctor.registrationDate.isNotEmpty())
                                referredPersonDoctor.registrationDateValue = AllUtil.getTimeFromTimeStamp(referredPersonDoctor.registrationDate)
                            _isLoading.value = 0
                        }
                        else
                            _isLoading.value = -2
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
        else{
            AndroidNetworking.get("https://www.uptodd.com/api/referredpatients/{referredPatientId}")
                .addPathParameter("referredPatientId", referredPersonId.toString())
                .addHeaders("Authorization","Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            val gson = Gson()
                            val type: Type = object : TypeToken<ReferredListItemPatient?>() {}.type
                            referredPersonPatient = gson.fromJson(response.get("data").toString(), type) as ReferredListItemPatient
                            referredPersonPatient.referralDateValue = AllUtil.getTimeFromTimeStamp(referredPersonPatient.referalDate)
                            if(referredPersonPatient.registrationDate!=null && referredPersonPatient.registrationDate.isNotEmpty())
                                referredPersonPatient.registrationDateValue = AllUtil.getTimeFromTimeStamp(referredPersonPatient.registrationDate)
                            _isLoading.value = 0
                        }
                        else
                            _isLoading.value = -2
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

    fun setUp(doctor: Boolean) {
        if(doctor){
            referralId.value = referredPersonDoctor.id
            referralName.value = referredPersonDoctor.name
            referralEmail.value = referredPersonDoctor.mail
            referralDate.value = getDateFromTime(referredPersonDoctor.referralDateValue)
            referralStatus.value = referredPersonDoctor.referralStatus
            referralPhone.value = referredPersonDoctor.phone
            referralCity.value = referredPersonDoctor.city
            if(referredPersonDoctor.registrationDate!=null && referredPersonDoctor.registrationDate.isNotEmpty())
                referralRegistrationDate.value = getDateFromTime(referredPersonDoctor.registrationDateValue)
            else
                referralRegistrationDate.value = "Unknown"
            babyName.value = "Baby name"
            babyDOB.value = "Baby DOB"
            layout1Field1.value = referredPersonDoctor.totalReferred.toString()
            layout2Field1.value = "Referrals enrolled here"
            layout3Field1.value = referredPersonDoctor.totalAmountEarned.toString()
            _babyVisibility.value = View.INVISIBLE
            _doctorVisibility.value = View.VISIBLE
            layout1Text1Text.value = "Total Referrals"
            layout2Text2Text.value = "Referrals Enrolled"
            layout3Text3Text.value = "Amount Referred"
        }
        else{
            referralId.value = referredPersonPatient.id
            referralName.value = referredPersonPatient.patientName
            referralEmail.value = referredPersonPatient.patientMail
            referralDate.value = getDateFromTime(referredPersonPatient.referralDateValue)
            referralStatus.value = referredPersonPatient.referralStatus
            referralPhone.value = referredPersonPatient.patientPhone
            referralRegistrationDate.value = referredPersonPatient.registrationDate
            babyName.value = referredPersonPatient.babyGender
            babyDOB.value = referredPersonPatient.babyDOB
            layout1Field1.value = "Rating here"
            layout2Field1.value = referredPersonPatient.enrolledDuration.toString()
            layout3Field1.value = referredPersonPatient.amountSubmitted.toString()
            _babyVisibility.value = View.VISIBLE
            _doctorVisibility.value = View.INVISIBLE
            layout1Text1Text.value = "Rating"
            layout2Text2Text.value = "Enrolled Duration"
            layout3Text3Text.value = "Amount Submitted"
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateFromTime(time: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(Date(time))
    }

}