package com.example.hackerstudent.api


import com.google.gson.annotations.SerializedName

data class MotivationItem(
    @SerializedName("a") val a: String,
    @SerializedName("h") val h: String,
    @SerializedName("q") val q: String
)