package com.uptodd.uptoddapp.datamodel.freeparentinglogin


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("created_at") val createdAt: Any?,
    @SerializedName("email") val email: String,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("progress") var progress: Any?
)