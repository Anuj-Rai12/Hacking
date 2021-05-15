package com.uptodd.uptoddapp.database.media.music

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "music_files")
data class MusicFiles(

    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "image")
    var image: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "language")
    var language: String? = null,

    @ColumnInfo(name = "file")
    var file: String? = null,

    @ColumnInfo(name = "playtimeInMinutes")
    var playtimeInMinutes:Int = 0,

    @ColumnInfo(name = "filePath")
    var filePath:String = ""

) : Serializable