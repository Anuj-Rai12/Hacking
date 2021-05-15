package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expected_outcome_table")
data class ExpectedOutcomes(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String = "",
    @ColumnInfo(name = "url")
    val url: String = "",
    @ColumnInfo(name = "image")
    val image: String = "",
    @ColumnInfo(name = "period")
    val period: Int = 0,
    @ColumnInfo(name = "description")
    val description: String = ""
)