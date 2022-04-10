package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FormQuestionResponse(@SerializedName("status")
                                val status:String,
                                @SerializedName("data")
                                val response: ArrayList<Response>,
                                @SerializedName("message")
                                val message:String?
                                ):Serializable