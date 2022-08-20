package com.uptodd.uptoddapp.api.freeparentingapi.login

import com.uptodd.uptoddapp.datamodel.changepass.ChangePasswordRequest
import com.uptodd.uptoddapp.datamodel.changepass.ChangePasswordResponse
import com.uptodd.uptoddapp.datamodel.forgetpass.ForgetPassRequest
import com.uptodd.uptoddapp.datamodel.forgetpass.ForgetPassResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {

    @POST(FilesUtils.FreeParentingApi.Login)
    suspend fun setLoginApi(@Body request: FreeParentingLoginRequest): Response<FreeParentingResponse>


    @POST(FilesUtils.FreeParentingApi.forget_Pass)
    suspend fun forgetPass(@Body request: ForgetPassRequest): Response<ForgetPassResponse>

    @POST(FilesUtils.FreeParentingApi.CHANGE_PASS)
    suspend fun changePassword(@Body response: ChangePasswordRequest): Response<ChangePasswordResponse>


}