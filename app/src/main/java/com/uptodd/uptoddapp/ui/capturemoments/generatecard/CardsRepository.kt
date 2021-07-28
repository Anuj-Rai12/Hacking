package com.uptodd.uptoddapp.ui.capturemoments.generatecard

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.capturemoments.generatecard.Card
import com.uptodd.uptoddapp.database.capturemoments.generatecard.GenerateCardDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class CardsRepository(private val database: GenerateCardDatabase)
{

    fun getCardsByCategoryKey(key:String?): LiveData<List<Card>> {
        Log.d("div","CardsRepostiory L22 $key")
        return database.generateCardDatabaseDao.getCardsFromRoom(key!!)
    }

    suspend fun refreshCards(type: String?,token:String?)
    {
        withContext(Dispatchers.IO){
            AndroidNetworking.get("https://www.uptodd.com/api/frames/$type")
                .addHeaders("Authorization","Bearer $token")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d("div", "CardsRepository L36 ${response.get("status")} -> ${
                                response.get(
                                    "data")
                            }")

                            var cardsListData = response.get("data") as JSONArray
                            Log.d("div","${response.get("data")}")
                            val list = ArrayList<Card>()
                            var i = 0
                            while (i < cardsListData.length()) {
                                val obj = cardsListData.get(i) as JSONObject
                                list.add(Card(obj.getLong("id"),obj.getString("frameImage"), "",obj.getString("category")))
                                Log.d("div", "CardsRepository L35 ${list.get(0)}")
                                i++
                            }
                            AsyncTask.execute {
                                database.generateCardDatabaseDao.insertCardsToRoom(list)
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d("div",
                            "CardsRepository L45 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )

                    }

                })

        }
    }
}