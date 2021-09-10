package com.example.hackerstudent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.hackerstudent.repos.CourseRepository
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val query: Query
) :
    ViewModel() {
    val getTodayQuote = courseRepository.getTodayQuote().asLiveData()
    val courseTodayFirst= courseRepository.getCourseOnlyThree(query).asLiveData()
}