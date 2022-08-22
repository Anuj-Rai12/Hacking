package com.uptodd.uptoddapp.ui.freeparenting.purchase.repo

import com.uptodd.uptoddapp.api.freeparentingapi.upgrade.UpgradeApi
import com.uptodd.uptoddapp.datamodel.freeparentinglogin.FreeParentingResponse
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeRequest
import com.uptodd.uptoddapp.ui.freeparenting.login.repo.LoginRepository
import com.uptodd.uptoddapp.utils.ApiResponseWrapper
import com.uptodd.uptoddapp.utils.buildApi
import com.uptodd.uptoddapp.utils.deserializeFromJson
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Retrofit

class CourseUpgradeRepository(retrofit: Retrofit) {

    private val api = buildApi<UpgradeApi>(retrofit)

    fun postUpGradeRequest(request: UpgradeRequest) = flow {
        emit(ApiResponseWrapper.Loading("Upgrading our account"))
        val data = try {
            val res = api.doCourseUpgrade(request)
            if (res.isSuccessful) {
                res.body()?.let {
                    ApiResponseWrapper.Success(it)
                } ?: ApiResponseWrapper.Error(LoginRepository.err_for_response, null)
            } else {
                deserializeFromJson<FreeParentingResponse>(res.errorBody()?.string())?.let {
                    ApiResponseWrapper.Error("${it.message}", null)
                } ?: ApiResponseWrapper.Error(LoginRepository.err, null)
            }
        } catch (e: Exception) {
            ApiResponseWrapper.Error(null, e)
        }
        emit(data)
    }.flowOn(IO)
}