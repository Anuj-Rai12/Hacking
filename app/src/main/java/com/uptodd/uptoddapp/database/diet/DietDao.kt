package com.uptodd.uptoddapp.database.diet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.diet.Diet

@Dao
interface DietDao {
    @Insert
    suspend fun insertAll(diets : List<Diet>)

    @Query("select * from diet_table")
    fun getAll() : LiveData<List<Diet>>
}