package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class DevelopmentTracker(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: Any,
    @SerializedName("status")
    val status: String
)