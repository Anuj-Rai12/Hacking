package com.uptodd.uptoddapp.ui.freeparenting.login.repo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.uptodd.uptoddapp.api.freeparentingapi.login.LoginApi
import com.uptodd.uptoddapp.datamodel.changepass.ChangePasswordRequest
import com.uptodd.uptoddapp.datamodel.forgetpass.ForgetPassRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingLoginRequest
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.LoginSingletonResponse
import com.uptodd.uptoddapp.utils.*
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

    companion object {
        const val err = "Oops Something Went Wrong"
        const val err_for_response = "Failed to process response"
    }

    fun getSignInUserInfo(request: FreeParentingLoginRequest) = flow {
        emit(ApiResponseWrapper.Loading(null))
        val data = try {
            val response = api.setLoginApi(request)
            setLogCat("Api_res", "${response.body()}")
            setLogCat("Api_res", "Error -> ${response.errorBody()}")
            setLogCat("Api_res", response.message())
            if (response.isSuccessful) {
                response.body()?.let {
                    LoginSingletonResponse.getInstance().setLoginResponse(it)
                    return@let if (setPresence(request, it.data.id.toLong())) {
                        ApiResponseWrapper.Success(null)
                    } else {
                        ApiResponseWrapper.Error("Failed to Save the Response Data Store ", null)
                    }
                } ?: ApiResponseWrapper.Error(err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(response.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)


    fun forgetPass(request: ForgetPassRequest) = flow {
        emit(ApiResponseWrapper.Loading(null))
        val data = try {
            val response = api.forgetPass(request)
            setLogCat("FORGET_PASS", "${response.errorBody()}")
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(response.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)


    fun changePass(request: ChangePasswordRequest) = flow {
        emit(ApiResponseWrapper.Loading(null))
        val data = try {
            val response = api.changePassword(request)
            setLogCat("CHANGE_PASS", "${response.errorBody()}")
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(response.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(e, null)
        }
        emit(data)
    }.flowOn(IO)


    private fun setPresence(response: FreeParentingLoginRequest, id: Long): Boolean {
        val edit = preferences.edit()
        edit.apply {
            putString(FilesUtils.DATASTORE.LoginType, FilesUtils.DATASTORE.FREE_LOGIN)
            putString(FilesUtils.DATASTORE.LoginResponse.email, response.email)
            putString(FilesUtils.DATASTORE.LoginResponse.password, response.pass)
            putLong(FilesUtils.DATASTORE.LoginResponse.userId, id)
            return commit()
        }
    }


    fun getLoginPreferences(): FreeParentingLoginRequest? {
        val email = preferences.getString(FilesUtils.DATASTORE.LoginResponse.email, "") ?: ""
        val password = preferences.getString(FilesUtils.DATASTORE.LoginResponse.password, "") ?: ""
        val id = preferences.getLong(FilesUtils.DATASTORE.LoginResponse.userId, 0)
        if (checkUserInput(password) || checkUserInput(email)) {
            return null
        }
        LoginSingletonResponse.getInstance().setLoginId(id)
        return FreeParentingLoginRequest(
            email = email,
            pass = password
        )
    }


    fun removeLoginValue() {
        preferences.edit().remove(FilesUtils.DATASTORE.LoginResponse.email).apply()
        preferences.edit().remove(FilesUtils.DATASTORE.LoginResponse.password).apply()
        preferences.edit().remove(FilesUtils.DATASTORE.LoginResponse.userId).apply()
    }
}