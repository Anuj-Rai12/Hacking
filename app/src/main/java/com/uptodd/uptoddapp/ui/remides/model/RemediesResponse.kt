package com.uptodd.uptoddapp.ui.remides.model


import com.google.gson.annotations.SerializedName

data class RemediesResponse(
    @SerializedName("data") val data: List<Data>,
    @SerializedName("message") val message: Any?,
    @SerializedName("status") val status: String
)