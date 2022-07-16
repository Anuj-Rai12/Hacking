package com.uptodd.uptoddapp.utils

sealed class ApiResponseWrapper<T>(
    val data: T? = null,
    val exception: Exception? = null
) {
    class Loading<T>(data: T?) : ApiResponseWrapper<T>(data)
    class Success<T>(data: T) : ApiResponseWrapper<T>(data)
    class Error<T>(data: T? = null, exception: Exception?) : ApiResponseWrapper<T>(data, exception)
}