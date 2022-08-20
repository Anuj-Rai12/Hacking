package com.uptodd.uptoddapp.datamodel.changepass


import com.google.gson.annotations.SerializedName

data class ChangePasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("newPassword") val newPassword: String
)