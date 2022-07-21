package com.uptodd.uptoddapp.api.freeparentingapi.updateprogress

import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressRequest
import com.uptodd.uptoddapp.datamodel.updateuserprogress.UpdateUserProgressResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdateUserProgressApi {

    @POST(FilesUtils.FreeParentingApi.UPDATE_PROGRESS)
    suspend fun getUpdateProgress(@Body request: UpdateUserProgressRequest): Response<UpdateUserProgressResponse>

}