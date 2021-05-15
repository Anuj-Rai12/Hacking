package com.uptodd.uptoddapp.ui.capturemoments.selecttype

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.uptodd.uptoddapp.database.capturemoments.selecttype.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SelectTypeViewModel(application: Application,val token:String?) : AndroidViewModel(application)
{


    /*val photoType1=PhotoType(1,"https://www.linkpicture.com/q/baby.png","Family")
    val photoType2=PhotoType(2,"https://www.linkpicture.com/q/baby.png","Baby")
    val photoType3=PhotoType(3,"https://www.linkpicture.com/q/baby.png","Parent")
    val photoType4=PhotoType(4,"http://mars.jpl.nasa.gov/msl-raw-images/msss/01000/mcam/1000ML0044631120305209E02_DXXX.jpg","Celebration")
*/
    /*private var _photoTypeList= MutableLiveData<List<PhotoType>>()
    val photoTypeList: LiveData<List<PhotoType>>
        get() = _photoTypeList

    fun getAllPhotoTypes()
    {
        AndroidNetworking.get("https://uptodd.com/api/framescategory")        //replace music by blog in L54 and L55
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.d("div", "SelectTypeViewModel L38 ${response.get("status")} -> ${response.get("data")}")
                        parseJSONCategories(response.get("data") as JSONArray)

                    }
                }

                override fun onError(anError: ANError?) {
                    Log.d("div", "SelectTypeViewModel L45 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                    )

                }

            })
    }
    private fun parseJSONCategories(categoriesListData: JSONArray) {
        Log.d("div","SelectTypeViewModel L52 $categoriesListData")
        val list=ArrayList<PhotoType>()
        var i=0
        while (i< categoriesListData.length())
        {
            val obj=categoriesListData.get(i) as JSONObject
            list.add(PhotoType(obj.getLong("id"),obj.getString("categoryImage"),obj.getString("category")))
            Log.d("div", "SelectTypeViewModel L73 ${list.get(0)}")
            i++
        }
        _photoTypeList.value=list

    }*/


    private val viewModelJob= Job()
    private val viewModelScope= CoroutineScope(viewModelJob+Dispatchers.Main)

    private val database=getDatabase(application)
    private val photoTypeRepository=PhotoTypeRepository(database)

    var isRepositoryEmpty=MutableLiveData<Boolean>()



    init {
        viewModelScope.launch {
            photoTypeRepository.refreshPhotoTypes(token)
        }
    }
    val photoTypeList=photoTypeRepository.photoTypes

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun refresh()
    {
        viewModelScope.launch {
            photoTypeRepository.refreshPhotoTypes(token)
        }
    }
}