package com.uptodd.uptoddapp.datamodel.freeparentinglogin


import com.google.gson.annotations.SerializedName

data class FreeParentingResponse(
    @SerializedName("data") val data: Data,
    @SerializedName("message") val message: Any?,
    @SerializedName("status") val status: String
)