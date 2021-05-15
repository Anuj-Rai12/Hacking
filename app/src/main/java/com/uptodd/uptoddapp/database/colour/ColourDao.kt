package com.uptodd.uptoddapp.database.colour

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.color.Colour

@Dao
interface ColourDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(colours: List<Colour>)

    @Query("SELECT * FROM colour_table")
    fun getAll(): LiveData<List<Colour>>

}