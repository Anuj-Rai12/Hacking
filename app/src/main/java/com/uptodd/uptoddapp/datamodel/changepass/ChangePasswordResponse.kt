package com.uptodd.uptoddapp.datamodel.changepass


import com.google.gson.annotations.SerializedName

data class ChangePasswordResponse(
    @SerializedName("data") val data: ChangePasswordResponseBody,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: String
)