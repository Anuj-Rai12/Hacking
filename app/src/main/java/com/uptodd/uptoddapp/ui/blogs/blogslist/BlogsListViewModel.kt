package com.uptodd.uptoddapp.ui.blogs.blogslist

import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.uptodd.uptoddapp.database.UptoddDatabase
import com.uptodd.uptoddapp.database.blogs.BlogCategories
import com.uptodd.uptoddapp.database.blogs.Blogs
import com.uptodd.uptoddapp.utilities.AllUtil
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class BlogsListViewModel(blogDatabase: UptoddDatabase) : ViewModel() {

    var dpi: String = ""
    var token: String? = null

    private val blogDao = blogDatabase.blogDao
    private val categoriesDao = blogDatabase.categoryDao

    private var _categoriesList = MutableLiveData<ArrayList<BlogCategories?>>()
    val categoriesList: LiveData<ArrayList<BlogCategories?>>
        get() = _categoriesList

    var blogsList = MutableLiveData<MutableList<Blogs?>>()


    var pageNumber = -1
    var isLoading = false
    var categoryId: Long = -1
    var isLoadingDialogVisible = MutableLiveData<Boolean>()


    fun getAllCategories() {
        val userId=AllUtil.getUserId()
        AndroidNetworking.get("https://www.uptodd.com/api/category/{blogs}?userId=$userId")
            .addPathParameter("blogs", "blog")
            .addHeaders("Authorization", "Bearer $token")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        Log.d(
                            "div",
                            "BlogsListViewModel L46 ${response.get("status")} -> ${response.get("data")}"
                        )
                        parseJSONCategories(response.get("data") as JSONArray)

                    }
                    //_isLoading.value = false
                }

                override fun onError(anError: ANError?) {
                    Log.d(
                        "div",
                        "BlogsListViewModel L55 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                    )
                }

            })
    }

    private fun parseJSONCategories(categoriesListData: JSONArray) {
        Log.d("div", "BlogsListViewModel L68 $categoriesListData")
        val list = ArrayList<BlogCategories?>()

        //For "All blogs" category
        list.add(BlogCategories(categoryId = -1, categoryName = "All Blogs"))

        var i = 0
        while (i < categoriesListData.length()) {
            val obj = categoriesListData.get(i) as JSONObject
            list.add(
                BlogCategories(
                    categoryId = obj.getLong("id"),
                    categoryName = obj.getString("category")
                )
            )
            Log.d("div", "BlogsListViewModel L73 ${list.get(0)}")
            i++
        }

        viewModelScope.launch {
            categoriesDao.insertAll(list)
        }

        _categoriesList.value = list
        isLoadingDialogVisible.value = false

    }

    fun getBlogListByCategoryId() {
        var link: String? = null
        if (categoryId == -1L)                         //for "All blogs" categories, the categoryId=-1
            link = "https://www.uptodd.com/api/blogs?page=${pageNumber + 1}"
        else
            link = "https://www.uptodd.com/api/blogs?page=${pageNumber + 1}&categoryId=$categoryId"
        Log.d("div", "BlogsListViewModel L88 $link")
        AndroidNetworking.get(link)
            .addHeaders("Authorization", "Bearer $token")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        parseJSONBlogs(response.get("data") as JSONArray)

                    }
                    //_isLoading.value = false
                }

                override fun onError(anError: ANError?) {
                    Log.d(
                        "div",
                        "BlogsListViewModel L93 API error: ${anError?.response} --- ${anError?.errorCode} --- ${anError?.errorBody} --- ${anError?.errorDetail} "
                    )
                }

            })
    }

    private fun parseJSONBlogs(blogsListData: JSONArray) {
        Log.d("div", "BlogsListViewModel L120 ${blogsListData.length()}")
        val list = ArrayList<Blogs?>()
        var i = 0
        val appendable = "https://www.uptodd.com/images/app/android/thumbnails/blogs/$dpi/"
        while (i < blogsListData.length()) {
            val obj = blogsListData.get(i) as JSONObject

            list.add(
                Blogs(
                    obj.getLong("id"),
                    categoryId,
                    appendable + obj.getString("thumbnail") + ".webp",
                    "https://uptodd.com/" + obj.getString("blogUrl"),
                    obj.getString("title")
                )
            )
            i++
        }

        viewModelScope.launch {
            blogDao.insertAll(list)
        }


        //TODO remove these 3 lines later
//        i=0
//        while(i++<9 && list.size>0)
//            list.add(list[0])


        if (pageNumber == -1) {
            blogsList.value = list
            isLoadingDialogVisible.value = false
        } else {
            blogsList.value!!.addAll(list)
        }
        if (blogsListData.length() > 0)
            pageNumber++
//        Log.d("div", "BlogsListViewModel L129 ${_blogsList.value?.get(0)}")
    }

    fun loadMore(recyclerAdapter: BlogsAdapter, size: Int) {
        Log.d("div", "BlogsListViewModel L146 ${blogsList.value?.size}")
        blogsList.value?.add(null)
        blogsList.value?.size?.minus(1)?.let { recyclerAdapter.notifyItemInserted(it) }
        getBlogListByCategoryId()
        Log.d("div", "BlogsListViewModel L149 ${blogsList.value?.size}")
        val handler = Handler()
        handler.postDelayed({
            //_blogsList.value?.size?.minus(1)?.let { _blogsList.value?.removeAt(it) }
            blogsList.value?.removeAt(size)
            Log.d("div", "BlogsListViewModel L153 ${blogsList.value?.size}")
            val scrollPosition: Int? = blogsList.value?.size
            //scrollPosition?.let { recyclerAdapter.notifyItemRemoved(it) }
            recyclerAdapter.notifyItemRemoved(size)

            var currentSize = scrollPosition

            recyclerAdapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }

}