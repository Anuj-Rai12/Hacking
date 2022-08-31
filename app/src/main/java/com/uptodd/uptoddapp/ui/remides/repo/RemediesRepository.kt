package com.uptodd.uptoddapp.ui.remides.repo

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.uptodd.uptoddapp.ui.remides.model.RemediesResponse
import com.uptodd.uptoddapp.utilities.AllUtil

class RemediesRepository {

    private val remediesUrl = "https://uptodd.com/api/homeRemedies"


    fun getRemediesResponse(
        success: (response: RemediesResponse) -> Unit,
        error: (e: Throwable?, data: String?) -> Unit
    ) {
        val addAuth = AllUtil.getAuthToken()

        AndroidNetworking.get(remediesUrl)
            .addHeaders("Authorization", "Bearer $addAuth")
            .addQueryParameter("userId", "${AllUtil.getUserId()}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsObject(
                RemediesResponse::class.java,
                object : ParsedRequestListener<RemediesResponse> {
                    override fun onResponse(response: RemediesResponse?) {
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


}