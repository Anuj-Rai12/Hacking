package com.uptodd.uptoddapp.api.freeparentingapi

import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {

    @POST(FilesUtils.FreeParentingApi.Login)
    suspend fun setLoginApi(@Body request: FreeParentingLoginRequest): Response<FreeParentingResponse>

}