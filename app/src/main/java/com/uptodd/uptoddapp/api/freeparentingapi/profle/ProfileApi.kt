package com.uptodd.uptoddapp.api.freeparentingapi.profle

import com.uptodd.uptoddapp.datamodel.changeprofie.ChangeProfileRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface ProfileApi {


    @GET(FilesUtils.FreeParentingApi.Get_user_detail)
    suspend fun getProfileDetail(@Query("id") id: Long): Response<FreeParentingResponse>


    @PUT(FilesUtils.FreeParentingApi.Get_profile_section_update)
    suspend fun changeProfileApi(@Body request: ChangeProfileRequest): Response<FreeParentingResponse>


}