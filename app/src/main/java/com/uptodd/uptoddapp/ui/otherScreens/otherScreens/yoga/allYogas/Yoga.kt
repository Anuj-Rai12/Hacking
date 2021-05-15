package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "yoga_table")
data class Yoga(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "url")
    val url: String = "",
    @ColumnInfo(name = "image")
    val image: String = "",
    @ColumnInfo(name = "steps")
    val steps: String = "",
    @ColumnInfo(name = "description")
    val description: String = ""
)