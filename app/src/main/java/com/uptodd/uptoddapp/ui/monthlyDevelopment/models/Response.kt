package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Response(
    @SerializedName("questions")
    var questions: List<Question>,
    @SerializedName("type")
    var type: String
):Serializable