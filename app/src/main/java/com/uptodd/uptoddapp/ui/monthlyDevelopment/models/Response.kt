package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class Response(
    @SerializedName("questions")
    val questions: List<Question>,
    @SerializedName("type")
    val type: String
)