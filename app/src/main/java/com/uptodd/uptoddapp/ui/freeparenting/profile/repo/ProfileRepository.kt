package com.uptodd.uptoddapp.ui.freeparenting.profile.repo

import com.uptodd.uptoddapp.api.freeparentingapi.profle.ProfileApi
import com.uptodd.uptoddapp.datamodel.changeprofie.ChangeProfileRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.ui.freeparenting.login.repo.LoginRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.buildApi
import com.uptodd.uptoddapp.utils.deserializeFromJson
import com.uptodd.uptoddapp.utils.getEmojiByUnicode
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class ProfileRepository(retrofit: Retrofit) {

    private val api = buildApi<ProfileApi>(retrofit)


    fun getProfile(id: Long) = flow {
        emit(ApiResponseWrapper.Loading("loading profile.. ${getEmojiByUnicode(0x1F575)}"))
        val data = try {
            val response = api.getProfileDetail(id)
            if (response.isSuccessful) {
                response.body()?.let {
                    LoginSingletonResponse.getInstance().setLoginResponse(it)
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(LoginRepository.err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(response.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(LoginRepository.err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)


    fun changeProfileDetail(request: ChangeProfileRequest) = flow {
        emit(ApiResponseWrapper.Loading("updating profile.. ${getEmojiByUnicode(0x1F575)}"))
        val data = try {
            val response = api.changeProfileApi(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    LoginSingletonResponse.getInstance().setLoginResponse(it)
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(LoginRepository.err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(response.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(LoginRepository.err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)

}