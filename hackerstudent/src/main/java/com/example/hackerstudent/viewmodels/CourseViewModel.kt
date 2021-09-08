package com.example.hackerstudent.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.hackerstudent.repos.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(private val courseRepository: CourseRepository) :
    ViewModel() {
    val getTodayQuote = courseRepository.getTodayQuote().asLiveData()
}