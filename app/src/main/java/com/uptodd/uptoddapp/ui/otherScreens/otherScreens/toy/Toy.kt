package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.toy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "toys_table")
data class Toy(
    @ColumnInfo(name = "name")
    val name: String,
    @PrimaryKey(autoGenerate = false)
    val url: String,
    @ColumnInfo(name = "image")
    val image: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "minAge")
    val minAge: Int,
    @ColumnInfo(name = "maxAge")
    val maxAge: Int
)