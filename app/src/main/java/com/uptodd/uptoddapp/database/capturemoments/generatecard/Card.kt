package com.uptodd.uptoddapp.database.capturemoments.generatecard

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="card_table")
data class Card(
    @PrimaryKey(autoGenerate = true)
    var cardId:Long=0,

    @ColumnInfo(name="image_url")
    var imageURL:String?=null,

    @ColumnInfo(name="text")
    var text:String?=null,

    @ColumnInfo(name="category")
    var category:String?=null

)