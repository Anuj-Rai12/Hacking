package com.uptodd.uptoddapp.ui.account.account

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.account.Account
import com.uptodd.uptoddapp.sharedPreferences.UptoddSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AccountRepository(private val accountPreference: UptoddSharedPreferences) {

//    private val database = uptoddDatabase.accountDatabaseDao

    val accountDetails = MutableLiveData<Account>()

    fun getAccountDetails() {
        accountDetails.value = accountPreference.getAccountDetails()
//        return database.getAccountDetailsFromRoom()
    }

    suspend fun refreshAccountDetails(userId: String, token: String?) {
        withContext(Dispatchers.IO) {
            AndroidNetworking.get("https://uptodd.com/api/appusers/{userId}")
                .addPathParameter("userId", userId)
                .addHeaders("Authorization", "Bearer $token")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d(
                                "div",
                                "AccountRepository L33 ${response.get("status")} -> ${response.get("data")} "
                            )

                            var accountJSON =
                                (response.get("data") as JSONArray).get(0) as JSONObject
                            val account = Account(
                                accountJSON.getLong("id"),
                                accountJSON.getString("profileImageUrl"),
                                accountJSON.getString("name"),
                                accountJSON.getString("email"),
                                accountJSON.getString("phoneno"),
                                accountJSON.getString("address"),
                                accountJSON.getInt("parentingScore"),
                                10,
                                accountJSON.getInt("isNannyModeActive") == 1,
                                accountJSON.getString("nannyLoginId"),
                                accountJSON.getString("nannyLoginPassword"),
                                accountJSON.getString("financeMailId"),
                                accountJSON.getString("kidsDob"),
                                accountJSON.getString("kidsName"),
                                accountJSON.getString("kidsGender"),
                                accountJSON.getString("kidsPhoto"),
                                accountJSON.getString("whichParent"),
                                accountJSON.getString("motherStage"),
                                accountJSON.getLong("freeSessionAvailable"),
                                accountJSON.getLong("paidSessioncount"),
                                accountJSON.getLong("currentSubscribedPlan"),
                                accountJSON.getInt("subscriptionActive") != 0,
                                accountJSON.getString("subscriptionStartDate")
                            )


                            accountPreference.saveAccountDetails(account)

                            accountDetails.value = account

                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d(
                            "div",
                            "AccountRepository L55 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )

                    }

                })

        }
    }
}