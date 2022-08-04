package com.uptodd.uptoddapp.ui.freeparenting.content.repo

import com.uptodd.uptoddapp.api.freeparentingapi.freedemocontent.VideoContentApi
import com.uptodd.uptoddapp.api.freeparentingapi.updateprogress.UpdateUserProgressApi
import com.uptodd.uptoddapp.database.freeparenting.VideoContentDao
import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressRequest
import com.uptodd.uptoddapp.datamodel.videocontent.Content
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.buildApi
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class VideoContentRepository(retrofit: Retrofit, private val dao: VideoContentDao) {

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
        emit(ApiResponseWrapper.Loading("updating progress"))
        val data = try {
            val response = updateProgressApi.getUpdateProgress(request)
            val info = if (response.isSuccessful) {
                response.body()?.let {
                    if(it.data && it.status=="Success"){
                        ApiResponseWrapper.Success(it)
                    }else{
                        ApiResponseWrapper.Error("Failed to track your progress",null)
                    }
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

    fun getAllVideoFromDb() = flow {
        emit(ApiResponseWrapper.Loading("Loading Video content"))
        val data = try {
            val res = dao.getVideoContentList()
            ApiResponseWrapper.Success(res)
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun getInsetVideoFromDb(video: Content) = flow {
        emit(ApiResponseWrapper.Loading("Adding video content..."))
        val data = try {
            dao.insertVideoContentItem(video)
            ApiResponseWrapper.Success("Success fully Inserted")
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)




    fun deleteVideoFromDb() = flow {
        emit(ApiResponseWrapper.Loading("Loading video content..."))
        val data = try {
            dao.deleteAllVideoContent()
            ApiResponseWrapper.Success("Success fully Deleted")
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)



}