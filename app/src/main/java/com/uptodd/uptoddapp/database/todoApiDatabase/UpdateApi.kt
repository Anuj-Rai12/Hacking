package com.uptodd.uptoddapp.database.todoApiDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "update_api_table")
data class UpdateApi(

    @ColumnInfo(name = "swipe_date")
    var swipeDate: String = "",

    @ColumnInfo(name = "date")
    val date: String,       // fetch date (primary key is date + type combination)

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "activity_id")
    var activityId: String,

    @ColumnInfo(name = "row_id")
    var rowId: Int,

    @ColumnInfo(name = "is_updated")
    var isUpdated: Boolean = false,

    @PrimaryKey(autoGenerate = true)  // simply an id
    val workManagerId: Int = 0,
)