package com.uptodd.uptoddapp.database.score

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_table")
data class Score(

    @PrimaryKey(autoGenerate = false)
    val id: Int,

    @ColumnInfo(name = "total_todos")
    var totalTodos: Int = 0,

//    @ColumnInfo(name = "type")
//    val type : String = "not added",

    @ColumnInfo(name = "completed_todos")
    var completedTodos: Int = 0

)