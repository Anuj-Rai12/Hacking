package com.uptodd.uptoddapp.datamodel.freeparentinglogin


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("email") val email: String,
    @SerializedName("id") val id: Int,
    @SerializedName("kidsDob") val kidsDob: String,
    @SerializedName("kidsGender") val kidsGender: String,
    @SerializedName("kidsName") val kidsName: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("progress") val progress: Int,
    @SerializedName("status") val status: String
)