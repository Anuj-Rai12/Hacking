package com.uptodd.uptoddapp.datamodel.changeprofie


import com.google.gson.annotations.SerializedName

data class ChangeProfileRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("kidsDob") val kidsDob: String? = null,
    @SerializedName("kidsGender") val kidsGender: String? = null,
    @SerializedName("kidsName") val kidsName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null
)