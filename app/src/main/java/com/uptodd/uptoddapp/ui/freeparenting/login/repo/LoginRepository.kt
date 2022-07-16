package com.uptodd.uptoddapp.ui.freeparenting.login.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.uptodd.uptoddapp.api.freeparentingapi.LoginApi
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.FilesUtils
import com.uptodd.uptoddapp.utils.buildApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class LoginRepository(retrofit: Retrofit, application: Application) {
    private val api = buildApi<LoginApi>(retrofit)


    private val preferences: SharedPreferences by lazy {
        application.getSharedPreferences(
            FilesUtils.DATASTORE.PERSISTENCE_Login,
            Context.MODE_PRIVATE
        )
    }
    private val err = "Oops Something Went Wrong"

    fun getSignInUserInfo(request: FreeParentingLoginRequest) = flow {
        emit(ApiResponseWrapper.Loading(null))
        val data = try {
            val response = api.setLoginApi(request)
            if (response.isSuccessful) {
                response.body()?.let { res ->
                    return@let if (setPresence(res)) {
                        LoginSingletonResponse.getInstance().apply {
                            setLoginResponse(res)
                            setLoginRequest(request)
                        }
                        ApiResponseWrapper.Success(null)
                    } else {
                        ApiResponseWrapper.Error(err, null)
                    }
                } ?: ApiResponseWrapper.Error(err, null)
            } else {
                ApiResponseWrapper.Error(err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)

    private fun setPresence(response: FreeParentingResponse): Boolean {
        val edit = preferences.edit()
        edit.apply {
            putString(FilesUtils.DATASTORE.LoginType, FilesUtils.DATASTORE.FREE_LOGIN)
            putString(FilesUtils.DATASTORE.LoginResponse.email, response.data.email)
            putString(FilesUtils.DATASTORE.LoginResponse.name, response.data.name)
            putString(FilesUtils.DATASTORE.LoginResponse.phone, response.data.phone)
            return commit()
        }
    }
}