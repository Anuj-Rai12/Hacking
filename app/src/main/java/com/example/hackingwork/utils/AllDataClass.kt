package com.example.hackingwork.utils

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.Gson

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


object Helper {
    fun serializeToJson(bmp: GetCourseContent): String? {
        val gson = Gson()
        return gson.toJson(bmp)
    }

    // Deserialize to single object.
    fun deserializeFromJson(jsonString: String?): GetCourseContent? {
        val gson = Gson()
        return gson.fromJson(jsonString, GetCourseContent::class.java)
    }
}
data class GetCourseContent(
    val thumbnail:String?=null,
    val previewvideo:String?=null,
    val module:Map<String,Module>?=null
)