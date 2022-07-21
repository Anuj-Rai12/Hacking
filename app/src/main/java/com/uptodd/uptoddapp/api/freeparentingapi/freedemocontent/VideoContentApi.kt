package com.uptodd.uptoddapp.api.freeparentingapi.freedemocontent

import com.uptodd.uptoddapp.datamodel.videocontent.VideoContentList
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.GET

interface VideoContentApi {

    @GET(FilesUtils.FreeParentingApi.DemoContent)
    suspend fun getVideoContentApi(): Response<VideoContentList>

}