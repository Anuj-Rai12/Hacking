package com.uptodd.uptoddapp.datamodel.videocontent


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Video_Content")
data class Content(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("time") val time: Int?,
    @SerializedName("type") val type: String,
    @SerializedName("url") val url: String
)