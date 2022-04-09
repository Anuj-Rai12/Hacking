package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("answer")
    var answer: String,
   @SerializedName("id")
    val id: Int,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("question")
    val question: String,
    val type:String?=null,
)