package com.example.hackerstudent.repos

import com.example.hackerstudent.api.RestApi
import com.example.hackerstudent.utils.MySealed
import com.example.hackerstudent.utils.UploadFireBaseData
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import javax.inject.Inject

class CourseRepository @Inject constructor(private val restApi: RestApi) {

    fun getTodayQuote() = flow {
        emit(MySealed.Loading("Getting Today Quote"))
        val data = try {
            val response = restApi.getInfo()
            MySealed.Success(response)
        } catch (e: Exception) {
            MySealed.Error(e, null)
        } catch (e: HttpException) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)

    fun getCourseOnlyThree(query: Query) = flow {
        emit(MySealed.Loading("Loading Featured Course"))
        val data = try {
            val info = query.get().await()
            val courseData: MutableList<UploadFireBaseData> = mutableListOf()
            info.forEach {
                courseData.add(it.toObject(UploadFireBaseData::class.java))
            }
            MySealed.Success(courseData)
        } catch (e: Exception) {
            MySealed.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)


}