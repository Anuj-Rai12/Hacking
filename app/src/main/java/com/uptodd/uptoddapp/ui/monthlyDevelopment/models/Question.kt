package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Question(
    @SerializedName("answer")
    var answer: String,
   @SerializedName("id")
    val id: Int,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("question")
    val question: String
):Serializable