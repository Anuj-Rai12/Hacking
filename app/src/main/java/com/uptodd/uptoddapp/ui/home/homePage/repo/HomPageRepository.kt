package com.uptodd.uptoddapp.ui.home.homePage.repo

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.uptodd.uptoddapp.ui.home.homePage.reviewmodel.ProgramReviewRequest
import com.uptodd.uptoddapp.ui.home.homePage.reviewmodel.ProgramReviewResponse
import com.uptodd.uptoddapp.utilities.AllUtil

class HomPageRepository {

    private val reviewUrl = "https://uptodd.com/api/appusers/rateApp"

    fun postResponseItem(
        requestBody: ProgramReviewRequest,
        success: (response: ProgramReviewResponse) -> Unit,
        error: (e: Throwable?, data: String?) -> Unit
    ) {
        val userId = AllUtil.getUserId()
        val addAuth = AllUtil.getAuthToken()
        AndroidNetworking.post("$reviewUrl?userId=$userId")
            .addHeaders("Authorization", "Bearer $addAuth")
            .addBodyParameter(requestBody)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsObject(
                ProgramReviewResponse::class.java,
                object : ParsedRequestListener<ProgramReviewResponse> {
                    override fun onResponse(response: ProgramReviewResponse?) {
                        response?.let {
                            success.invoke(it)
                        } ?: kotlin.run {
                            error.invoke(null, "Cannot Process Response")
                        }
                    }

                    override fun onError(anError: ANError?) {
                        error.invoke(anError?.cause, null)
                    }
                })
    }


    companion object {
        enum class AndroidNetworkingResponseWrapper {
            SUCCESS,
            ERROR,
            LOADING
        }
    }
}