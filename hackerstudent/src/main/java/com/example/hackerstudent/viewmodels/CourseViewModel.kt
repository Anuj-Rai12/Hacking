package com.example.hackerstudent.viewmodels

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.hackerstudent.TAG
import com.example.hackerstudent.paginate.PaginationCourse
import com.example.hackerstudent.repos.CourseRepository
import com.example.hackerstudent.utils.GetConstStringObj
import com.google.firebase.firestore.CollectionReference
import com.razorpay.Checkout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val query: CollectionReference,
) :
    ViewModel() {
    val getTodayQuote = courseRepository.getTodayQuote().asLiveData()
    val courseTodayFirst =
        courseRepository.getCourseOnlyThree(query.limit(GetConstStringObj.Per_page.toLong()))
            .asLiveData()


    /*.whereArrayContains("targetaudience", search)
    .whereArrayContains("requirement", search)*/

    fun getSearchQuery(SearchQuery: String?) = Pager(
        PagingConfig(
            pageSize = GetConstStringObj.Per_page,
            enablePlaceholders = false
        )
    ) {
        val querySearch = if (SearchQuery != null) {
            Log.i(TAG, "getSearchQuery: ViewModel -> $SearchQuery")
            query.whereGreaterThanOrEqualTo(
                "fireBaseCourseTitle.coursename",
                SearchQuery
            ).limit(1.toLong())
        } else
            query.limit(1.toLong())

        PaginationCourse(querySearch)
    }.flow.cachedIn(viewModelScope)

    fun showPaymentOption(
        checkout: Checkout,
        fragmentActivity: FragmentActivity,
        jsonObject: JSONObject
    ) = courseRepository.showPaymentOption(checkout, fragmentActivity, jsonObject).asLiveData()


    val searchQuery = MutableStateFlow("")

}