package com.uptodd.uptoddapp.ui.home.homePage.reviewmodel


import com.google.gson.annotations.SerializedName

data class ProgramReviewRequest(
    @SerializedName("comment") val comment: String,
    @SerializedName("id") val id: Int,
    @SerializedName("rating") val rating: Int
)