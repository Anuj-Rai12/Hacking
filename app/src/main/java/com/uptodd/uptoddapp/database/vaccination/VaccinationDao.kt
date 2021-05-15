package com.uptodd.uptoddapp.database.vaccination

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.vaccination.Vaccination

@Dao
interface VaccinationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(v: List<Vaccination>)

    @Query("select * from vaccination_table")
    fun getAll(): LiveData<List<Vaccination>>
}