package com.uptodd.uptoddapp.api.freeparentingapi.profle

import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileApi {


    @GET(FilesUtils.FreeParentingApi.Get_user_detail)
    suspend fun getProfileDetail(@Query("id") id: Long): Response<FreeParentingResponse>

}