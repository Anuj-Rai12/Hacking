package com.uptodd.uptoddapp.database.blogs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blogs_table")
data class Blogs(
    @PrimaryKey(autoGenerate = true)
    var blogId: Long = 0,

    @ColumnInfo(name = "blogCategory")
    val blogCategory: Long,

    @ColumnInfo(name = "image_url")
    var imageURL: String? = null,

    @ColumnInfo(name = "blog_url")
    var blogURL: String? = null,

    @ColumnInfo(name = "title")
    var title: String? = null
)