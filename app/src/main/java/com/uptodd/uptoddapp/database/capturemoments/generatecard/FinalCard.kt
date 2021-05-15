package com.uptodd.uptoddapp.database.capturemoments.generatecard

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="final_card_table")
data class FinalCard(
    @PrimaryKey(autoGenerate = true)
    var cardId:Long=0,

    @ColumnInfo(name="final_card")
    var finalCard: Bitmap?=null,

    @ColumnInfo(name="final_card_frame_id")
    var finalCardFrameId: Long=0

)