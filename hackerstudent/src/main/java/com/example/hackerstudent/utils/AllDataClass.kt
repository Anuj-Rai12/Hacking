package com.example.hackerstudent.utils


import com.example.hackerstudent.api.Motivation
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


@IgnoreExtraProperties
data class UploadFireBaseData(
    val fireBaseCourseTitle: FireBaseCourseTitle? = null,
    val previewvideo: String? = null,
    val thumbnail: String? = null
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


sealed class CourseSealed {

    data class Image(
        val Id: String,
        val raw: Int
    ) : CourseSealed()

    data class Title(
        val title: String?,
        val motivation: Motivation?
    ) : CourseSealed()

    data class Course(
        val title: String,
        val fireBaseCourseTitle: List<FireBaseCourseTitle>
    ) : CourseSealed()
}
