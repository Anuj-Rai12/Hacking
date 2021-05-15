package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.diet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_table")
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "image")
    val image: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "period")
    val period: Int,
    @ColumnInfo(name = "type")
    val type: String
)