package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

data class Question(
    val answer: String,
    val id: Int,
    val options: List<String>,
    val question: String
)