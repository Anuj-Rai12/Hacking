package com.uptodd.uptoddapp.ui.todoScreens.ranking

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

class RankingViewModel(val uid: String?) : ViewModel() {

    private var _dailyScore = MutableLiveData<String>("0")
    val dailyScore: LiveData<String>
        get() = _dailyScore

    private var _weeklyScore = MutableLiveData<String>("0")
    val weeklyScore: LiveData<String>
        get() = _weeklyScore

    private var _monthlyScore = MutableLiveData<String>("0")
    val monthlyScore: LiveData<String>
        get() = _monthlyScore

    private var _essentialsScore = MutableLiveData<String>("0")
    val essentialsScore: LiveData<String>
        get() = _essentialsScore

    private var _ranking = MutableLiveData<String>("0")
    val ranking: LiveData<String>
        get() = _ranking


    init {
//        viewModelScope.launch {
//            _dailyScore.value = scoreDatabase.getScoreOfType(DAILY_TODO)
//            _weeklyScore.value = scoreDatabase.getScoreOfType(WEEKLY_TODO)
//            _monthlyScore.value = scoreDatabase.getScoreOfType(MONTHLY_TODO)
//            _essentialsScore.value = scoreDatabase.getScoreOfType(ESSENTIALS_TODO)
//        }

        getRankingUsingApi()
    }


    fun getRankingUsingApi() {
        if (uid == null) return
        viewModelScope.launch {
            AndroidNetworking.get("https://uptodd.com/api/activity/score/{userID}")        //replace music by blog in L54 and L55
                .addPathParameter("userID", uid)
                .addHeaders("Authorization", "Bearer ${AllUtil.getAuthToken()}")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
//                            val data = response.get("data") as JSONArray
//                            var i = 0
//                            _ranking.value = data.getString(4)
//                            while (i < data.length()) {
//                                val rankJson = data.get(i) as JSONObject
//                                when (rankJson.get("activityType")) {
//                                    "daily" -> _dailyScore.value = rankJson.getString("score")
//                                    "weekly" -> _weeklyScore.value = rankJson.getString("score")
//                                    "monthly" -> _monthlyScore.value = rankJson.getString("score")
//                                    "essentials" -> _essentialsScore.value =
//                                        rankJson.getString("score")
//                                }
//                                i++
//                            }

                            val data = response.get("data") as JSONObject
                            if (data.has("0")) {
                                val score_daily = data.get("0") as JSONObject
                                _dailyScore.value = score_daily.getString("score")
                            }
                            if (data.has("1")) {
                                val score_essentials = data.get("1") as JSONObject
                                _essentialsScore.value = score_essentials.getString("score")
                            }

                            if (data.has("2")) {
                                val score_monthly = data.get("2") as JSONObject
                                _monthlyScore.value = score_monthly.getString("score")
                            }

                            if (data.has("3")) {
                                val score_weekly = data.get("3") as JSONObject
                                _weeklyScore.value = score_weekly.getString("score")
                            }

                            if (data.has("ranking")) {
                                _ranking.value = data.getString("ranking")
                            }

                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d(
                            "ViewModel",
                            "API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )
                    }
                })
        }
    }


}