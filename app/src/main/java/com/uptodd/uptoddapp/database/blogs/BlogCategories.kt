package com.uptodd.uptoddapp.database.blogs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blogs_categories_table")
data class BlogCategories(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "categoryId")
    var categoryId: Long = 0,

    @ColumnInfo(name = "category_name")
    var categoryName: String? = null

)