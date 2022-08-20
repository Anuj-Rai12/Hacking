package com.uptodd.uptoddapp.datamodel.forgetpass


import com.google.gson.annotations.SerializedName

data class ForgetPassRequest(
    @SerializedName("email") val email: String
)