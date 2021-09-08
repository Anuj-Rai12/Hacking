package com.example.hackerstudent.repos

import com.example.hackerstudent.api.RestApi
import com.example.hackerstudent.utils.MySealed
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
}