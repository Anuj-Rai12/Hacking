package com.uptodd.uptoddapp.ui.freeparenting.content.repo

import com.uptodd.uptoddapp.api.freeparentingapi.freedemocontent.VideoContentApi
import com.uptodd.uptoddapp.api.freeparentingapi.updateprogress.UpdateUserProgressApi
import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressRequest
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.buildApi
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class VideoContentRepository(retrofit: Retrofit) {

    private val api = buildApi<VideoContentApi>(retrofit)
    private val updateProgressApi = buildApi<UpdateUserProgressApi>(retrofit)

    companion object {
       val error = Pair("Failed to get Response", "Oops something Went Wrong")
    }

    fun getVideoContent() = flow {
        emit(ApiResponseWrapper.Loading("Please Wait ${getEmojiByUnicode(0x1F575)}"))
        val data = try {
            val response = api.getVideoContentApi()
            val info = if (response.isSuccessful) {
                response.body()?.let {
                    return@let ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(error.second, null)
            } else {
                ApiResponseWrapper.Error(error.first, null)
            }
            info
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun updateUserProgress(request: UpdateUserProgressRequest) = flow {
        emit(ApiResponseWrapper.Loading("Please Wait"))
        val data = try {
            val response = updateProgressApi.getUpdateProgress(request)
            val info = if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(error.second, null)
            } else {
                ApiResponseWrapper.Error(error.first, null)
            }
            info
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


}