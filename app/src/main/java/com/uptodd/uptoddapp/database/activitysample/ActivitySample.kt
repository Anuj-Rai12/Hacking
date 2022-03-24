package com.uptodd.uptoddapp.database.activitysample

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(tableName = "activity_sample_table")
data class ActivitySample(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,
    @SerializedName("name")
    @ColumnInfo(name = "title")
    val title: String,
    @SerializedName("video")
    @ColumnInfo(name = "video")
    val video: String
):Serializable