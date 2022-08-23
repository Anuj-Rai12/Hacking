package com.uptodd.uptoddapp.api.freeparentingapi.feedback

import com.uptodd.uptoddapp.datamodel.feedback.FeedBackRequest
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FeedBackApi {

    @POST(FilesUtils.FreeParentingApi.feed_back_request)
    suspend fun getFeedBackRequest(@Body request: FeedBackRequest):
            Response<UpgradeResponse>


}