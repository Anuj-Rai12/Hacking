package com.uptodd.uptoddapp.datamodel.videocontent


import com.google.gson.annotations.SerializedName

data class Content(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: Int?,
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String
)