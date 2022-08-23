package com.uptodd.uptoddapp.ui.freeparenting.feedback.repo

import com.uptodd.uptoddapp.api.freeparentingapi.feedback.FeedBackApi
import com.uptodd.uptoddapp.datamodel.feedback.FeedBackRequest
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeResponse
import com.uptodd.uptoddapp.ui.freeparenting.daily_book.repo.VideoContentRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.buildApi
import com.uptodd.uptoddapp.utils.deserializeFromJson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class FeedBackRepository(retrofit: Retrofit) {
    private val api = buildApi<FeedBackApi>(retrofit)

    fun sendFeedBack(request: FeedBackRequest) = flow {
        emit(ApiResponseWrapper.Loading("Sending Response"))
        val data = try {
            val response = api.getFeedBackRequest(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponseWrapper.Success("Submitted your feed Back\nThanks for your efforts")
                } ?: ApiResponseWrapper.Error(VideoContentRepository.error.first, null)
            } else {
                val res = deserializeFromJson<UpgradeResponse>(response.errorBody()?.string())
                ApiResponseWrapper.Error("${res?.message}", null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

}