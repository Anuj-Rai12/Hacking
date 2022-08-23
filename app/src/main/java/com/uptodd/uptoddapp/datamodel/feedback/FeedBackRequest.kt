package com.uptodd.uptoddapp.datamodel.feedback


import com.google.gson.annotations.SerializedName

data class FeedBackRequest(
    @SerializedName("feedback") val feedback: String,
    @SerializedName("id") val id: Int
)