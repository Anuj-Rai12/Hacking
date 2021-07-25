package com.uptodd.uptoddapp.ui.webinars.webinarslist

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.webinars.DualWebinars
import com.uptodd.uptoddapp.database.webinars.WebinarCategories
import com.uptodd.uptoddapp.database.webinars.Webinars
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class WebinarsListViewModel(webinarsDatabase: UptoddDatabase) : ViewModel() {
    var token: String? = null
    var dpi: String = ""

    private val webinarsDatabaseDao = webinarsDatabase.webinarsDatabaseDao
    private val webinarsCategoryDao = webinarsDatabase.webinarCategoryDao

    var categoriesList =
        MutableLiveData<MutableList<WebinarCategories>>()

    var webinarsList = MutableLiveData<MutableList<Webinars?>>()

    var dualWebinarsList = ArrayList<DualWebinars?>()

    var pageNumber = -1
    var isLoading = false
    var categoryId: Long = -1
    var isLoadingDialogVisible = MutableLiveData<Boolean>()


    fun getAllCategories() {
        AndroidNetworking.get("https://www.uptodd.com/api/category/{webinars}")
            .addPathParameter("webinars", "webinar")
            .addHeaders("Authorization", "Bearer $token")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.d(
                            "div",
                            "WebinarsListViewModel L46 ${response.get("status")} -> ${response.get("data")}"
                        )
                        parseJSONCategories(response.get("data") as JSONArray)

                    }
                    //_isLoading.value = false
                }

                override fun onError(anError: ANError?) {
                    Log.d(
                        "div",
                        "WebinarsListViewModel L55 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                    )
                }

            })
    }

    private fun parseJSONCategories(categoriesListData: JSONArray) {

        val list = ArrayList<WebinarCategories>()

        //For "All webinars" category
        list.add(WebinarCategories(categoryId = -1, categoryName = "All Webinars"))

        var i = 0
        while (i < categoriesListData.length()) {
            val obj = categoriesListData.get(i) as JSONObject
            list.add(
                WebinarCategories(
                    categoryId = obj.getLong("id"),
                    categoryName = obj.getString("category")
                )
            )
            i++
        }

        viewModelScope.launch {
            webinarsCategoryDao.insertAll(list)
        }

        categoriesList.value = list
        isLoadingDialogVisible.value = false

    }

    fun getWebinarListByCategoryId() {
        var link: String? = null
        if (categoryId == -1L)                         //for "All webinars" categories, the categoryId=-1
            link = "https://www.uptodd.com/api/webinar?page=${pageNumber + 1}"
        else
            link = "https://www.uptodd.com/api/webinar?page=${pageNumber + 1}&categoryId=$categoryId"
        AndroidNetworking.get(link)
            .addHeaders("Authorization", "Bearer $token")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        parseJSONWebinars(response.get("data") as JSONArray)

                    }
                    //_isLoading.value = false
                }

                override fun onError(anError: ANError?) {
                    Log.d(
                        "div",
                        "WebinarsListViewModel L93 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                    )
                }

            })
    }

    private fun parseJSONWebinars(webinarsListData: JSONArray) {
        Log.d("div", "WebinarsListViewModel L102 ${webinarsListData.length()}")
        val list = ArrayList<Webinars?>()
        var i = 0

        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/webinars/$dpi/"
        while (i < webinarsListData.length()) {
            val obj = webinarsListData.get(i) as JSONObject
            Log.d("div", "WebinarsListViewModel L118 $obj")
            list.add(
                Webinars(
                    webinarId = obj.getLong("id"),
                    webinarCategoryId = categoryId,
                    imageURL = appendable + obj.getString("thumbnail") + ".webp",
                    webinarURL = obj.getString("videoUrl"),
                    title = obj.getString("title"),
                    description = obj.getString("smallDescription"),
                    date = obj.getString("publishedDate")
                )
            )
            Log.d("div", "WebinarsListViewModel L120 ${list.get(0)}")
            i++
        }


        viewModelScope.launch {
            webinarsDatabaseDao.insertAll(list)
        }


        if (pageNumber == -1) {
            webinarsList.value = list
            isLoadingDialogVisible.value = false
        } else {
            webinarsList.value!!.addAll(list)
        }
        if (webinarsListData.length() > 0)
            pageNumber++
//        Log.d("div", "WebinarsListViewModel L129 ${_webinarsList.value?.get(0)}")
    }

    fun loadMore(recyclerAdapter: WebinarsAdapter, size: Int) {
        dualWebinarsList.add(null)
        (dualWebinarsList.size.minus(1)).let { recyclerAdapter.notifyItemInserted(it) }
        getWebinarListByCategoryId()
        Log.d("div", "WebinarsListViewModel L135 ${recyclerAdapter.allWebinarsList}")
        val handler = Handler()
        handler.postDelayed({
            //dualWebinarsList.size?.minus(1)?.let { dualWebinarsList.removeAt(it) }
            dualWebinarsList.removeAt(size)
            val scrollPosition: Int? = dualWebinarsList.size
            //scrollPosition?.let { recyclerAdapter.notifyItemRemoved(it) }
            recyclerAdapter.notifyItemRemoved(size)
            var currentSize = scrollPosition

            recyclerAdapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }

}