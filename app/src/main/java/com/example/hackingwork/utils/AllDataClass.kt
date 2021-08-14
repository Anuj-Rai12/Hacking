package com.example.hackingwork.utils

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CreateUserAccount(
    val firstname: String? = null,
    val lastname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val ipaddress: String? = null,
    val password: String? = null,
    val bookmarks: Map<String, String>? = null,
    val courses: Map<String, String>? = null
)

data class Module(
    val module: String? = null,
    val video: Map<String, Video>? = null
)

data class Video(
    val title: String? = null,
    val uri: String? = null,
    val duration: String? =null,
    val assignment: Assignment? = null
)

data class Assignment(
    val title: String? = null,
    val uri: String? = null
)


