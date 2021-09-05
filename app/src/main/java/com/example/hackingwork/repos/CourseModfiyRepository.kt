package com.example.hackingwork.repos

import com.example.hackingwork.utils.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CourseModfiyRepository @Inject constructor(
) {
    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun uploadingCourse(courseContent: FireBaseCourseTitle, getCourseContent: GetCourseContent) =
        flow {
            emit(MySealed.Loading("${courseContent.coursename} is Creating.."))
            val data = try {
                val query = fireStore.collection(GetConstStringObj.Create_course)
                    .document(courseContent.coursename!!)
                val upload = UploadFireBaseData(
                    fireBaseCourseTitle = courseContent,
                    previewvideo = getCourseContent.previewvideo,
                    thumbnail = getCourseContent.thumbnail
                )
                query.set(upload).await()
                MySealed.Success(null)
            } catch (e: Exception) {
                MySealed.Error(null, e)
            }
            emit(data)
        }.flowOn(IO)

    fun uploadingVideoCourse(moduleKey: String, moduleValue: Module, courseName: String) = flow {
        emit(MySealed.Loading(null))
        val data = try {
            fireStore.collection(GetConstStringObj.Create_course).document(courseName)
                .collection(GetConstStringObj.Create_Module).document(moduleKey).set(moduleValue)
                .await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun updateNewModule(courseName: String) = flow {
        emit(MySealed.Loading("Updating $courseName"))
        val data = try {
            fireStore.collection(GetConstStringObj.Create_course).document(courseName)
                .update("fireBaseCourseTitle.lastdate", getDateTime()).await()
            MySealed.Success(null)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun addOtherNewVideo(courseName: String, moduleName: String, video: Video, videoName: String) =
        flow {
            emit(MySealed.Loading(null))
            val data = try {
                fireStore.collection(GetConstStringObj.Create_course).document(courseName)
                    .collection(GetConstStringObj.Create_Module).document(moduleName)
                    .update("video.$videoName", video).await()
                MySealed.Success(null)
            } catch (e: Exception) {
                MySealed.Error(null, e)
            }
            emit(data)
        }.flowOn(IO)

    fun unpaidModify(
        courseDetail: UnpaidClass?,
        udi: String,
        uploadType: Boolean,
        firstTimeAccount: Boolean
    ) = flow {
        val str = if (uploadType) "Adding Unpaid User" else "Deleting Unpaid User"
        val successStr =
            if (uploadType) "User is Added To Unpaid Folder" else "User is Deleted from Unpaid Folder"
        emit(MySealed.Loading(str))
        val data = try {
            val querySet = fireStore.collection(GetConstStringObj.UNPAID).document(udi)
            if (uploadType && firstTimeAccount)
                querySet.set(courseDetail!!).await()
            else if (uploadType && !firstTimeAccount)
                querySet.update(
                    "courses.${courseDetail?.courses?.keys?.first()}",
                    courseDetail?.courses?.values?.first()
                )
            else
                querySet.delete().await()

            MySealed.Success(successStr)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    fun modifyPaidUser(course: Map<String, CourseDetail>, udi: String, uploadType: Boolean) = flow {
        val str = if (uploadType) "Adding paid Course to User" else "Deleting paid Course to User"
        val successStr =
            if (uploadType) "Paid Course is added to User" else "User is Deleted paid course from User Folder"
        emit(MySealed.Loading(str))
        val data = try {
            val query = fireStore.collection(GetConstStringObj.USERS).document(udi)
            if (uploadType)
                query.update("courses.${course.keys.first()}", course.values.first()).await()
            else
                query.update("courses.${course.keys.first()}", FieldValue.delete()).await()

            MySealed.Success(successStr)
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

}