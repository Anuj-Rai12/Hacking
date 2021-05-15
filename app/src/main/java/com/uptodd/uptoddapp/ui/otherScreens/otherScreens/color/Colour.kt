package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.color

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colour_table")
data class Colour(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "image")
    val image: String,
    @ColumnInfo(name = "description")
    val description: String
)