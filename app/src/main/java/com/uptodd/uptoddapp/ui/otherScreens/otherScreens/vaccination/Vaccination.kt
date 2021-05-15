package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.vaccination

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccination_table")
data class Vaccination(
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @PrimaryKey(autoGenerate = false)
    val name: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "image")
    val image: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "age")
    val age: String
)