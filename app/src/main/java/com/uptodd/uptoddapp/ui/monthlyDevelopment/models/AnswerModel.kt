package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnswerModel(
    @SerializedName("answers")
    val answers:List<Response>):Serializable