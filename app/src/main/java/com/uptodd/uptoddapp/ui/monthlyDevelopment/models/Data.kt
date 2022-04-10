package com.uptodd.uptoddapp.ui.monthlyDevelopment.models

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("allResponses")
    val allResponses: ArrayList<AllResponse>,
    @SerializedName("isTrackerFormOpen")
    val isTrackerFormOpen: Int
)