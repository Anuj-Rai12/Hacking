package com.uptodd.uptoddapp.database.webinars

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webinars_categories_table")
data class WebinarCategories(

    @ColumnInfo(name = "category_id")
    var categoryId: Long = 0,

    @PrimaryKey(autoGenerate = false)
    var categoryName: String = ""

)