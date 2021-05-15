package com.uptodd.uptoddapp.database.webinars

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WebinarCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(webCatList: List<WebinarCategories>)

    @Query("select * from webinars_categories_table")
    fun getAll(): LiveData<List<WebinarCategories>>
}