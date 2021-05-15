package com.uptodd.uptoddapp.database.toys

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uptodd.uptoddapp.ui.otherScreens.otherScreens.toy.Toy

@Dao
interface ToysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(toys: List<Toy>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(toy: Toy)

    @Update
    suspend fun update(toy: Toy)

    @Delete
    suspend fun delete(toy: Toy)

    @Query("DELETE FROM toys_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM toys_table")
    fun getAll(): LiveData<List<Toy>>

    // for Testing Purpose
    @Query("SELECT * FROM toys_table WHERE name = :name")
    fun getByName(name: String): Toy?

    @Insert
    fun insertAllToys(toys: List<Toy>)

    @Delete
    fun deleteToy(toy: Toy)

    @Query("SELECT * FROM toys_table")
    fun getAllToys(): List<Toy>

}