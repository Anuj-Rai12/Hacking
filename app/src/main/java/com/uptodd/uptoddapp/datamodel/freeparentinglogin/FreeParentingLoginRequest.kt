package com.uptodd.uptoddapp.datamodel.freeparentinglogin

import com.google.gson.annotations.SerializedName

data class FreeParentingLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val pass: String,
    @SerializedName("device") val device: String = DeviceType.ANDROID.name,
)

enum class DeviceType {
    ANDROID
}