package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("answer")
    val answer: String,
   @SerializedName("id")
    val id: Int,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("questions")
    val question: String
)