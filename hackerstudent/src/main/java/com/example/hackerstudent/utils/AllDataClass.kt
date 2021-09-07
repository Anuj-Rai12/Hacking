package com.example.hackerstudent.utils

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CreateUserAccount(
    val firstname: String? = null,
    val lastname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val ipaddress: String? = null,
    val password: String? = null,
    val token: String? = null,
    var id: String? = null,
    val bookmarks: Map<String, CourseDetail>? = null,
    val courses: Map<String, CourseDetail>? = null
)
data class CourseDetail(
    val course: String? = null,
    val data: String? = null,
    val purchase: String? = null
)