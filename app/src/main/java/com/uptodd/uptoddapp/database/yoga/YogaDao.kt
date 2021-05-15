package com.uptodd.uptoddapp.database.yoga

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.yoga.allYogas.Yoga

@Dao
interface YogaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(yogas: List<Yoga>)

    @Query("select * from yoga_table")
    fun getAll(): LiveData<List<Yoga>>
}