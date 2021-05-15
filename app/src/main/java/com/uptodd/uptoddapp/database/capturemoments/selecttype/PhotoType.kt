package com.uptodd.uptoddapp.database.capturemoments.selecttype

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="photo_type_table")
data class PhotoType(
    @PrimaryKey(autoGenerate = true)
    var photoTypeId:Long=0,

    @ColumnInfo(name="image_url")
    var imageURL:String?=null,

    @ColumnInfo(name="title")
    var title:String?=null,

)