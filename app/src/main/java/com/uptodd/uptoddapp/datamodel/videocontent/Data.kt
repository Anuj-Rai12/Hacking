package com.uptodd.uptoddapp.datamodel.videocontent


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("content") val content: List<Content>,
    @SerializedName("section") val section: String
)