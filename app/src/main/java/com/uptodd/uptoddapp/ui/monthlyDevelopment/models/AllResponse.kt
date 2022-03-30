package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

data class AllResponse(
    val created_at: String,
    val id: Int,
    val month: Int,
    val response: List<Response>,
    val tips: String,
    val updated_at: String,
    val user_id: Int
)