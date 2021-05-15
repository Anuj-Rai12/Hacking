package com.uptodd.uptoddapp.doctor.dashboard

import android.util.Log
import android.view.Menu
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uptodd.uptoddapp.database.account.DoctorAccount
import com.uptodd.uptoddapp.database.webinars.Webinars
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type

class DoctorDashboardViewModel : ViewModel() {

    lateinit var editMenu: Menu
    private var doctorAccount = DoctorAccount()
    private var newDoctorAccountDetails = DoctorAccount()

    var isNavigationDone = false

    private var _webinars = MutableLiveData<ArrayList<Webinars>>()
    val webinars: LiveData<ArrayList<Webinars>>
        get() = _webinars

    var dpi: String = ""

    var doctorName = MutableLiveData<String>()
    var doctorEmail = MutableLiveData<String>()
    var doctorPhone = MutableLiveData<String>()
    var doctorWhatsapp = MutableLiveData<String>()
    var bankAccountNumber = MutableLiveData<String>()
    var bankName = MutableLiveData<String>()
    var ifscCode = MutableLiveData<String>()
    var accountHolderName = MutableLiveData<String>()
    var amountInBank = MutableLiveData<String>()
    var pendingAmount = MutableLiveData<String>()
    var amountByReferringDoctor = MutableLiveData<String>()
    var totalAmount = MutableLiveData<String>()
    var amountByReferringPatient = MutableLiveData<String>()
    var password = ""

    var doctorsReferred = MutableLiveData<String>()
    var doctorsEnrolled = MutableLiveData<String>()
    var patientsEnrolled = MutableLiveData<String>()
    var patientsReferred = MutableLiveData<String>()

    private var _isLoading: MutableLiveData<Int> = MutableLiveData()
    val isLoading: LiveData<Int>
        get() = _isLoading

    var apiError = ""

    init {
        getDoctorDetails()
        _isLoading.value = 1
    }

    fun reInit() {
        getDoctorDetails()
        _isLoading.value = 1
    }

    private fun getWebinars() {
        //TODO change this to webinars when they are ready
        AndroidNetworking.get("https://uptodd.com/api/blogs?page=0")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        parseJSONWebinars(response.get("data") as JSONArray)
                    }
                    _isLoading.value = 0
                }

                override fun onError(anError: ANError?) {
                    apiError = anError!!.message!!
                    AllUtil.logApiError(anError)
                    _isLoading.value = -1
                }

            })
    }

    private fun parseJSONWebinars(jsonArray: JSONArray) {
        Log.d("div", "Size ${jsonArray.length()}")
        val list = ArrayList<Webinars>()
        var i = 0
        //TODO change this to webinars when they are ready
        val appendable = "https://uptodd.com/images/app/android/thumbnails/blogs/$dpi/"
        while (i < 4) {
            if (jsonArray.length() == i)
                break
            val obj = jsonArray.get(i) as JSONObject
//            Log.d("div","BlogsListViewModel L116 $obj")
            list.add(
                Webinars(
                    webinarId = obj.getLong("id"),
                    imageURL = appendable + obj.getString("thumbnail") + ".webp",
                    webinarURL = "https://uptodd.com/" + obj.getString("blogUrl"),
                    title = obj.getString("title")
                )
            )
            i++
        }
        _webinars.value = list
    }

    fun initializeVariables() {
        doctorName.value = doctorAccount.name
        doctorEmail.value = doctorAccount.mail
        doctorPhone.value = doctorAccount.phone
        doctorWhatsapp.value = doctorAccount.whatsapp
        bankName.value = doctorAccount.bankName
        amountInBank.value = formatString(doctorAccount.amountInBank)
        pendingAmount.value = formatString(doctorAccount.pendingAmount)
        amountByReferringDoctor.value = formatString(doctorAccount.amountByReferringDoctor)
        amountByReferringPatient.value = formatString((doctorAccount.amountByReferringPatient))
        totalAmount.value = formatString(doctorAccount.totalAmount)

        hideBankAccountNumber()

        ifscCode.value = doctorAccount.ifscCode
        accountHolderName.value = doctorAccount.accountHolderName
        password = doctorAccount.password

        doctorsReferred.value = doctorAccount.totalDoctorReferred.toString()
        doctorsEnrolled.value = doctorAccount.totalDoctorEnrolled.toString()
        patientsEnrolled.value = doctorAccount.totalPatientEnrolled.toString()
        patientsReferred.value = doctorAccount.totalPatientReferred.toString()

    }

    fun showBankAccountNumber() {
        bankAccountNumber.value = doctorAccount.bankAccountNo
    }

    fun hideBankAccountNumber() {
        val acNoLen = doctorAccount.bankAccountNo.length
        val acno = doctorAccount.bankAccountNo
        if (acNoLen > 6)
            bankAccountNumber.value = acno.replaceRange(2, acNoLen - 2, "X".repeat(acNoLen - 4))
        else
            bankAccountNumber.value = "XXXX"
    }

    private fun getDoctorDetails() {
        AndroidNetworking.get("https://uptodd.com/api/doctor/{doctorId}")
            .addPathParameter("doctorId", AllUtil.getDoctorId().toString())
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.i("docRes", response.toString())
                        val gson = Gson()
                        val type: Type = object : TypeToken<DoctorAccount?>() {}.type
                        doctorAccount =
                            gson.fromJson(response.get("data").toString(), type) as DoctorAccount
                        newDoctorAccountDetails = DoctorAccount(doctorAccount)
                        getWebinars()
                    } else
                        _isLoading.value = -2
                }

                override fun onError(anError: ANError?) {
                    if (anError!!.errorCode == 0)
                        apiError = "Connection Timeout!"
                    else
                        apiError = anError.message.toString()
                    _isLoading.value = -2
                }
            })
    }

    private fun formatString(amount: Float): String {
        return String.format("%3.2f", amount)
    }

    fun updateAccount() {
        newDoctorAccountDetails.mail = doctorEmail.value
        newDoctorAccountDetails.phone = doctorPhone.value
        newDoctorAccountDetails.whatsapp = doctorWhatsapp.value
        newDoctorAccountDetails.bankAccountNo = bankAccountNumber.value
        newDoctorAccountDetails.ifscCode = ifscCode.value
        newDoctorAccountDetails.accountHolderName = accountHolderName.value
        newDoctorAccountDetails.bankName = bankName.value

        Log.i("val", "Changed -> ${bankName.value}")

        if (newDoctorAccountDetails == doctorAccount) {
            _isLoading.value = 12
        } else {
            val jsonObject = JSONObject()
            jsonObject.put("mail", newDoctorAccountDetails.mail)
            jsonObject.put("phone", newDoctorAccountDetails.phone)
            jsonObject.put("whatsapp", newDoctorAccountDetails.whatsapp)
            jsonObject.put("bankAccountNo", newDoctorAccountDetails.bankAccountNo)
            jsonObject.put("ifscCode", newDoctorAccountDetails.ifscCode)
            jsonObject.put("accountHolderName", newDoctorAccountDetails.accountHolderName)
            jsonObject.put("bankName", newDoctorAccountDetails.bankName)
            _isLoading.value = 11

            Log.i("passing", "$jsonObject")

            AndroidNetworking.put("https://uptodd.com/api/doctor/{doctorId}")
                .addPathParameter("doctorId", AllUtil.getDoctorId().toString())
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            if (response.getString("status") == "Success") {
                                Log.i("response", "$response")
                                doctorAccount = DoctorAccount(newDoctorAccountDetails)
                                initializeVariables()
                                _isLoading.value = 10
                            } else {
                                apiError = response.getString("message")
                                _isLoading.value = -1
                            }
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

    }

}