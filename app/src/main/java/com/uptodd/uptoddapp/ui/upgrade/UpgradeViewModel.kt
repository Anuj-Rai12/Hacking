package com.uptodd.uptoddapp.ui.upgrade

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.api.getUserId
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import com.uptodd.uptoddapp.utilities.AllUtil
import org.json.JSONObject


class UpgradeViewModel: ViewModel(
    ) {

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private var _upList=MutableLiveData<ArrayList<UpgradeItem>>()
    val upList:LiveData<ArrayList<UpgradeItem>>
    get() = _upList
    private var _isPaymentDone=MutableLiveData<Boolean>()
    val isPaymentDone:LiveData<Boolean>
    get() = _isPaymentDone


   companion object
   {
       var paymentStatus=""
       var paymentDone=false
   }



 fun getUpgradeList(context: Context) {
        var stage=UptoddSharedPreferences.getInstance(context).getStage()

     stage = if(stage=="post birth" ||stage=="postnatal") {
         "postnatal"
     } else {
         "prenatal"
     }

     val country=if(UptoddSharedPreferences.getInstance(context).getPhone()?.startsWith("+91")!!)
         "india"
     else
         "row"


        _isLoading.value=true
            AndroidNetworking.get("https://uptodd.com/api/nonPremiumAppusers/productDetails?stage=$stage&country=$country")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {

                        val upgradeList = AllUtil.getAllUpgrade(response.get("data").toString())
                        _upList.value=upgradeList
                        _isLoading.value=false
                    } else {
                    }
                }

                override fun onError(error: ANError) {
                    Log.i("upgradlist error", error.errorBody)
                    _isLoading.value=false
                }
            })
    }

    fun checkIsPaymentDone(context: Context)
    {
        val userId= getUserId(context)
        _isLoading.value=true
        AndroidNetworking.get("https://uptodd.com/api/nonPremiumAppusers/isPaymentDone/$userId")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success")
                    {
                        val data=response.get("data") as JSONObject

                        if(data.getInt("paymentDone")==0){
                            _isPaymentDone.value=false
                            Log.d("payment done","false")
                        }
                        if(data.getInt("paymentDone")==1){
                            _isPaymentDone.value=true
                            Log.d("payment done","true")
                        }
                        _isLoading.value=false
                    } else {
                        _isPaymentDone.value=false
                        _isLoading.value= false
                    }
                }

                override fun onError(error: ANError) {
                    _isPaymentDone.value=false
                    _isLoading.value=false
                    Log.i("payment status error", error.errorBody)
                }
            })


    }


    fun notifySalesTeam(upgradeItem: UpgradeItem,context: Context)
    {
        val userId= getUserId(context)
        val json=JSONObject()
        json.put("stage",upgradeItem.stage)
        json.put("userId",userId)
        json.put("country",upgradeItem.country)
        json.put("productMonth",upgradeItem.productMonth)


        AndroidNetworking.post("https://uptodd.com/api/nonPremiumAppusers/upgrade")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .addJSONObjectBody(json)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {

                     Log.d("informed sales team","success")
                    } else {
                    }
                }

                override fun onError(error: ANError) {
                    Log.e("informed sales team ", error.errorBody)
                }
            })
    }

    fun savePaymentDetails(context: Context,paymentId:String,productMonth:Int)
    {
        val userId= getUserId(context)
        Log.d("userId", userId.toString())
        val json=JSONObject()
        json.put("userId",userId)
        json.put("paymentId",paymentId)
        json.put("productMonth",productMonth)

        AndroidNetworking.post("https://uptodd.com/api/nonPremiumAppusers/savePaymentDetails")
            .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
            .setPriority(Priority.HIGH)
            .addJSONObjectBody(json)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getString("status") == "Success") {

                        Log.d("payment saved","success")
                    } else {
                    }
                }

                override fun onError(error: ANError) {
                    Log.e("payment saved ", error.errorBody)
                }
            })
    }
}