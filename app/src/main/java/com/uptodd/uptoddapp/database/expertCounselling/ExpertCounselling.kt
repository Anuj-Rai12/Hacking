package com.uptodd.uptoddapp.database.expertCounselling

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "expert_counselling")
data class ExpertCounselling(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int?,
    @SerializedName("name")
    @ColumnInfo(name="title")
    val name: String?,
    @ColumnInfo(name = "tips")
    val tips: String?,
    @ColumnInfo(name = "session_date")
    val sessionDate:String?,
    @ColumnInfo(name = "date")
    @SerializedName("date")
    val date: String?,
    @ColumnInfo(name = "startTime")
    @SerializedName("startTime")
    val startTime:String?,
    @ColumnInfo(name="endTime")
    @SerializedName("endTime")
    val endTime:String?,
    @ColumnInfo(name="duration")
    @SerializedName("duration")
    val duration: String?,
    @ColumnInfo(name = "joiningLink")
    @SerializedName("joiningLink")
    val joiningLink:String?,
    @ColumnInfo(name="rescheduleLink")
    @SerializedName("rescheduleLink")
    val rescheduleLink:String?,
    @ColumnInfo(name="status")
    @SerializedName("status")
    val status:String?,
    @ColumnInfo(name="expectedDate")
    @SerializedName("expectedDate")
    val expectedDate:String?,
    @SerializedName("note")
    val note:String?

):Serializable