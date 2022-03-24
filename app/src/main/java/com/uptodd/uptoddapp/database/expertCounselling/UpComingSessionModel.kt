package com.uptodd.uptoddapp.database.expertCounselling

import com.google.gson.annotations.SerializedName
import java.io.Serializable
data class UpComingSessionModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("startTime")
    val startTime:String,
    @SerializedName("endTime")
    val endTime:String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("joiningLink")
    val joiningLink:String,
    @SerializedName("rescheduleLink")
    val rescheduleLink:String
):Serializable