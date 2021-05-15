package com.uptodd.uptoddapp.database.webinars

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webinars_table")
data class Webinars(

    @PrimaryKey(autoGenerate = false)
    var webinarId: Long = 0,

    @ColumnInfo(name = "webinarCategoryId")
    val webinarCategoryId: Long? = null,

    @ColumnInfo(name = "image_url")
    var imageURL: String? = null,

    @ColumnInfo(name = "webinar_url")
    var webinarURL: String? = null,

    @ColumnInfo(name = "title")
    var title: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "date")
    var date: String? = null

)