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
    val token: String? = null,
    var id: String? = null,
    val bookmarks: Map<String, CourseDetail>? = null,
    val courses: Map<String, CourseDetail>? = null
)

data class CourseDetail(
    val course: String? = null,
    val data: String? = null,
    val purchase: String? = null,
    val status: String? = null,
    val purchaseid: String? = null
)

data class UnpaidClass(
    var id: String? = null,
    val courses: Map<String, CourseDetail>? = null
)

data class Module(
    val module: String? = null,
    val video: Map<String, Video>? = null
)

data class Video(
    val title: String? = null,
    val uri: String? = null,
    val duration: String? = null,
    val assignment: Assignment? = null
)

data class Assignment(
    val title: String? = null,
    val uri: String? = null
)


object Helper {
    fun <T> serializeToJson(bmp: T): String? {
        val gson = Gson()
        return gson.toJson(bmp)
    }

    // Deserialize to single object.
    inline fun <reified T> deserializeFromJson(jsonString: String?): T? {
        val gson = Gson()
        return gson.fromJson(jsonString, T::class.java)
    }
}

data class GetCourseContent(
    val thumbnail: String? = null,
    val previewvideo: String? = null,
    val module: Map<String, Module>? = null
)

data class UserViewOnCourse(
    val bywhom: String? = null,
    val rateing: String? = null,
    val description: String? = null
)

data class FireBaseCourseTitle(
    val coursename: String? = null,
    val totalhrs: String? = null,
    val category: String? = null,
    val courselevel: String? = null,  //Snippet
    val lastdate: String? = null,
    val courseContent: GetCourseContent? = null,
    val requirement: List<String>? = null,
    val targetaudience: List<String>? = null,
    val totalprice: String? = null,
    val currentprice: String? = null,
    val review: UserViewOnCourse? = null
)

@IgnoreExtraProperties
data class UploadFireBaseData(
    val fireBaseCourseTitle: FireBaseCourseTitle? = null,
    val previewvideo: String? = null,
    val thumbnail: String? = null
)