package com.uptodd.uptoddapp.database.webinars

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="webinars_table")
data class DualWebinars(
    @PrimaryKey(autoGenerate = true)
    var webinarId:Long=0,

    @ColumnInfo(name="image_url1")
    var imageURL1:String?=null,

    @ColumnInfo(name="webinar_url1")
    var webinarURL1:String?=null,

    @ColumnInfo(name="title1")
    var title1:String?=null,

    @ColumnInfo(name="description1")
    var description1:String?=null,

    @ColumnInfo(name="date1")
    var date1:String?=null,

    @ColumnInfo(name="image_url2")
    var imageURL2:String?=null,

    @ColumnInfo(name="webinar_url2")
    var webinarURL2:String?=null,

    @ColumnInfo(name="title2")
    var title2:String?=null,

    @ColumnInfo(name="description2")
    var description2:String?=null,

    @ColumnInfo(name="date2")
    var date2:String?=null,

)