package com.example.hackingwork.repos

import com.example.hackingwork.utils.FireBaseCourseTitle
import com.example.hackingwork.utils.GetConstStringObj
import com.example.hackingwork.utils.MySealed
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CourseModfiyRepository @Inject constructor(
    private val fireStore: FirebaseFirestore
) {

    fun uploadingCourse(courseContent: FireBaseCourseTitle) = flow {
        emit(MySealed.Loading("${courseContent.coursename} is Creating.."))
        val data = try {
            val info = fireStore.collection(GetConstStringObj.Create_course)
                .document(courseContent.coursename!!).set(courseContent)
            info.await()
            MySealed.Success("${courseContent.coursename} is Created...")
        } catch (e: Exception) {
            MySealed.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

}