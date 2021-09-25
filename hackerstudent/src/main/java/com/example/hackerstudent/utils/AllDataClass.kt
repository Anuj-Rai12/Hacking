package com.example.hackerstudent.utils


import android.os.Parcelable
import com.example.hackerstudent.api.Motivation
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

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
    val bookmarks: Map<String, CoursePurchase>? = null,
    val courses: Map<String, CoursePurchase>? = null
)

data class LocalCoursePurchase(
    val coursePurchase: CoursePurchase,
    val messages: String
)


data class CoursePurchase(
    val course: String? = null,//Course Name
    val data: String? = null,
    val purchase: String? = null,//Amount
    val status: String? = null,
    val purchaseid: String? = null// Purchase Id
)


@IgnoreExtraProperties
data class UploadFireBaseData(
    val fireBaseCourseTitle: FireBaseCourseTitle? = null,
    val previewvideo: String? = null,
    val thumbnail: String? = null,
    var id: String? = null
)

@Parcelize
data class SendSelectedCourse(
    val coursename: String? = null,
    val totalhrs: String? = null,
    val category: String? = null,
    val courselevel: String? = null,  //Snippet
    val lastdate: String? = null,
    val requirement: List<String>? = null,
    val targetaudience: List<String>? = null,
    val totalprice: String? = null,
    val currentprice: String? = null,
    val review: UserViewOnCourse? = null,
    val previewvideo: String? = null,
    val thumbnail: String? = null
) : Parcelable

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

@Parcelize
data class UserViewOnCourse(
    val bywhom: String? = null,
    val rateing: String? = null,
    val description: String? = null
) : Parcelable

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
data class VersionControl(
    //Version Code
    val version: String? = null,
    //Update Url
    val updateurl: String? = null,
    //Contact Detail
    val whatsapp: String? = null,
    val insta: String? = null,
    val facebook: String? = null,
    val twitter: String? = null,
)


sealed class PaidCourseSealed {
    data class CourseList(
        val title: String,
        val uploadFireBaseData: List<UploadFireBaseData>?
    ) : PaidCourseSealed()

    data class User(
        val name: String,
        val layout: Int
    ) : PaidCourseSealed()
}

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
        val fireBaseCourseTitle: List<UploadFireBaseData>
    ) : CourseSealed()
}

sealed class ProfileDataClass {
    data class ImageHeader(
        val email: String,
        val firstname: String,
        val lastname: String
    ) : ProfileDataClass()

    data class Title(
        val title: String
    ) : ProfileDataClass()

    data class OptionFooter(
        val data: String
    ) : ProfileDataClass()
}

sealed class CoursePreview {

    data class VideoCourse(
        val videoPreview: String,
        val title: String,
        val thumbnail: String
    ) : CoursePreview()

    data class CourseRatingAndOther(
        val rating: String,
        val totalHrs: String
    ) : CoursePreview()

    data class ArrayClass(
        val title: String,
        val requirement: List<String>? = null,
        val targetAudience: List<String>? = null,
    ) : CoursePreview()

    data class CoursePrice(
        val currAmt: String,
        val mrp: String,
        val title: String
    ) : CoursePreview()

    data class ReviewSection(
        val data: UserViewOnCourse
    ) : CoursePreview()
}

data class RequirementData(
    val list: String
)