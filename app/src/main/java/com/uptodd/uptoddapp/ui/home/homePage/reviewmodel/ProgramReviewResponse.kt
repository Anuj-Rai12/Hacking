package com.uptodd.uptoddapp.ui.home.homePage.reviewmodel


import com.google.gson.annotations.SerializedName

data class ProgramReviewResponse(
    @SerializedName("data") val `data`: String,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String
)