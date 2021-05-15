package com.uptodd.uptoddapp.ui.order.orderlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.order.Order
import org.json.JSONArray
import org.json.JSONObject

class OrderViewModel:ViewModel()
{


    val order1= Order(1,"111-222-333-444",1,"MyProduct",2,true,
        "05022020","https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")

    private var _allOrderList=MutableLiveData<List<Order>>()
    val allOrderList: LiveData<List<Order>>
        get() = _allOrderList

    var token:String?=null

    var isLoadingDialogVisible=MutableLiveData<Boolean>()

    var userId:String=""

    fun getOrdersFromDatabase() {
        if(_allOrderList.value==null) {
            AndroidNetworking.get("https://uptodd.com/api/appusers/products/{userId}")
                .addPathParameter("userId", userId)
                .addHeaders("Authorization", "Bearer $token")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d("div",
                                "OrderViewModel L47 ${response.get("status")} -> ${response.get("data")}")

                            var cardsListData = response.get("data") as JSONArray
                            val list = ArrayList<Order>()
                            var i = 0
                            while (i < cardsListData.length()) {
                                val obj = cardsListData.get(i) as JSONObject
                                if (obj.getBoolean("deliveryStatus")) {
                                    list.add(Order(obj.getLong("id"),
                                        obj.getLong("orderNo").toString(),
                                        obj.getLong("monthNo"),
                                        obj.getString("productName"),
                                        obj.getLong("quantity"),
                                        obj.getBoolean("deliveryStatus"),
                                        obj.getString("deliveryDate"),
                                        obj.getString("detailsPdfUrl"),
                                        obj.getString("description")))
                                } else {
                                    list.add(Order(obj.getLong("id"),
                                        obj.getLong("orderNo").toString(),
                                        obj.getLong("monthNo"),
                                        obj.getString("productName"),
                                        obj.getLong("quantity"),
                                        obj.getBoolean("deliveryStatus"),
                                        obj.getString("deliveryExpectedDate"),
                                        obj.getString("detailsPdfUrl"),
                                        obj.getString("description")))
                                }
                                Log.d("div", "OrderViewModel L78 ${list.get(i)}")
                                i++
                            }
                            _allOrderList.value = list
                            isLoadingDialogVisible.value = false
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d("div",
                            "OrderViewModel L86 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )

                    }

                })
        }
    }

    var isExtendSubscriptionRequestMade:Boolean=false
    fun requestExtendSubscription() {

        AndroidNetworking.post("https://uptodd.com/api/appusers/products/{userId}")
            .addPathParameter("userId",userId)
            .addHeaders("Authorization","Bearer $token")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d("div", "OrderViewModel L63 ${response?.get("status")}")
                    if (response != null && response.get("status") == "Success") {
                        Log.d("div", "OrderViewModel L65 ${response.get("status")}")
                        if(response.get("status")=="Success")
                            isExtendSubscriptionRequestMade=true
                        isLoadingDialogVisible.value = false

                    }
                }

                override fun onError(error: ANError?) {
                    isLoadingDialogVisible.value=false
                    Log.d("div", "OrderViewModel L67 $error")
                    if (error!!.getErrorCode() != 0) {
                        Log.d("div", "onError errorCode : " + error.getErrorCode())
                        Log.d("div", "onError errorBody : " + error.getErrorBody())
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    } else {
                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                        Log.d("div", "onError errorDetail : " + error.getErrorDetail())
                    }
                }

            })
    }
}