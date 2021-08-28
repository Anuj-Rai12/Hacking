package com.example.hackingwork.repos

import com.example.hackingwork.utils.*
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

}