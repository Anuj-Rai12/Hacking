package com.uptodd.uptoddapp.ui.capturemoments.selecttype

import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.capturemoments.selecttype.PhotoType
import com.uptodd.uptoddapp.database.capturemoments.selecttype.PhotoTypeDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


class PhotoTypeRepository(private val database: PhotoTypeDatabase)
{

    val photoTypes: LiveData<List<PhotoType>> = database.photoTypeDatabaseDao.getPhotoTypesFromRoom()

    suspend fun refreshPhotoTypes(token: String?)
    {
        withContext(Dispatchers.IO){
            AndroidNetworking.get("https://uptodd.com/api/framescategory")        //replace music by blog in L54 and L55
                .addHeaders("Authorization","Bearer $token")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            Log.d("div", "PhotoTypeRepository L26 ${response.get("status")} -> ${
                                response.get(
                                    "data")
                            }")

                            var categoriesListData = response.get("data") as JSONArray
                            val list = ArrayList<PhotoType>()
                            var i = 0
                            while (i < categoriesListData.length()) {
                                val obj = categoriesListData.get(i) as JSONObject
                                list.add(PhotoType(obj.getLong("id"),
                                    obj.getString("categoryImage"),
                                    obj.getString(
                                        "category")))
                                Log.d("div", "PhotoTypeRepository L35 ${list.get(0)}")
                                i++
                            }
                            AsyncTask.execute {
                                database.photoTypeDatabaseDao.insertPhotoTypesToRoom(list)
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        Log.d("div",
                            "PhotoTypeRepository L45 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                        )

                    }

                })

        }
    }
}