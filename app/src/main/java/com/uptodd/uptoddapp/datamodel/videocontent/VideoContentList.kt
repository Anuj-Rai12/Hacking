package com.uptodd.uptoddapp.datamodel.videocontent


import com.google.gson.annotations.SerializedName

data class VideoContentList(
    @SerializedName("data") val data: List<Data>,
    @SerializedName("message") val message: Any?,
    @SerializedName("status") val status: String
)