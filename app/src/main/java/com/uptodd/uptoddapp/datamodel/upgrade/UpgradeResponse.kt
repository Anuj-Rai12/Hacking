package com.uptodd.uptoddapp.datamodel.upgrade


import com.google.gson.annotations.SerializedName

data class UpgradeResponse(
    @SerializedName("data") val data: UpdGradeData,
    @SerializedName("message") val message: Any?,
    @SerializedName("status") val status: String
)