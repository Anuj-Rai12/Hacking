package com.uptodd.uptoddapp.datamodel.freeparentinglogin

import com.google.gson.annotations.SerializedName

data class FreeParentingLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("mobileCode") val mobileCode: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)