package com.uptodd.uptoddapp.database.webinars

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao

interface WebinarsDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(webinars: List<Webinars?>)

    @Query("select * from webinars_table")
    fun getAll(): LiveData<List<Webinars?>>

    @Query("select * from webinars_table where  webinarCategoryId = :catId")
    fun getAllByCategory(catId: Long): LiveData<List<Webinars?>>

}