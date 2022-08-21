package com.uptodd.uptoddapp.api.freeparentingapi.upgrade

import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeRequest
import com.uptodd.uptoddapp.datamodel.upgrade.UpgradeResponse
import com.uptodd.uptoddapp.utils.FilesUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UpgradeApi {

    @POST(FilesUtils.FreeParentingApi.upgrade_Request)
    suspend fun doCourseUpgrade(@Body request: UpgradeRequest): Response<UpgradeResponse>

}