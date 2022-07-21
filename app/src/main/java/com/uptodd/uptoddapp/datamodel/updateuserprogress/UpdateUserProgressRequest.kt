package com.uptodd.uptoddapp.datamodel.updateuserprogress


import com.google.gson.annotations.SerializedName

data class UpdateUserProgressRequest(
    @SerializedName("progress") val progress: Int,
    @SerializedName("userId") val userId: Int
)