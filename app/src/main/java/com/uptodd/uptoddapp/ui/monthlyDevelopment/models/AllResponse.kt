package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class AllResponse(
    @SerializedName("created_at")
    val created_at: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("month")
    val month: Int,
    @SerializedName("response")
    val response: List<Response>,
    @SerializedName("tips")
    val tips: String,
    @SerializedName("updated_at")
    val updated_at: String,
    @SerializedName("user_id")
    val user_id: Int
){
    fun getAllQuestions():Response{
        val questions=ArrayList<Question>()
        response.forEach {
            questions.addAll(it.questions)
        }
        return Response(questions,"$month Form")
    }
}