package com.uptodd.uptoddapp.ui.otherScreens.otherScreens.outcomes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expected_outcome_table")
data class ExpectedOutcomes(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name")
    var name: String = "",
    @ColumnInfo(name = "url")
    var url: String = "",
    @ColumnInfo(name = "image")
    var image: String = "",
    @ColumnInfo(name = "period")
    var period: Int = 0,
    @ColumnInfo(name = "description")
    var description: String = ""
)