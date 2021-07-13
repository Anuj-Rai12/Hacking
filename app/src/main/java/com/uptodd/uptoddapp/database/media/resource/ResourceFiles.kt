package com.uptodd.uptoddapp.database.media.resource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "resource_files")
data class ResourceFiles(

    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "filePath")
    var filePath:String = ""


) : Serializable