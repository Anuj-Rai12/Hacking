package com.uptodd.uptoddapp.datamodel.updateuserprogress


import com.google.gson.annotations.SerializedName

data class UpdateUserProgressResponse(
    @SerializedName("data") val data: Boolean,
    @SerializedName("message") val message: Any?,
    @SerializedName("status") val status: String
)